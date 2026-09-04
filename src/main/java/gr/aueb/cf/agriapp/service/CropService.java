package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.enums.ActivityType;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.agriapp.core.filters.CropFilters;
import gr.aueb.cf.agriapp.core.filters.Paginated;
import gr.aueb.cf.agriapp.core.specifications.CropSpecification;
import gr.aueb.cf.agriapp.dto.CropInsertDTO;
import gr.aueb.cf.agriapp.dto.CropReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.CropUpdateDTO;
import gr.aueb.cf.agriapp.mapper.Mapper;
import gr.aueb.cf.agriapp.model.Crop;
import gr.aueb.cf.agriapp.model.Farmer;
import gr.aueb.cf.agriapp.model.FieldActivity;
import gr.aueb.cf.agriapp.model.Parcel;
import gr.aueb.cf.agriapp.model.static_data.CropType;
import gr.aueb.cf.agriapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CropService implements ICropService {

    private final CropRepository cropRepository;
    private final ParcelRepository parcelRepository;
    private final CropTypeRepository cropTypeRepository;
    private final FieldActivityRepository fieldActivityRepository;
    private final FarmerRepository farmerRepository;
    private final Mapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CropReadOnlyDTO saveCrop(CropInsertDTO dto, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        Farmer farmer = getFarmer(username);
        Parcel parcel = getOwnedParcel(dto.parcelUuid(), farmer);
        CropType cropType = getCropType(dto.cropTypeId());

        Crop saved = cropRepository.save(mapper.mapToCropEntity(dto, parcel, cropType));
        log.info("Crop with uuid={} created on parcel={}", saved.getUuid(), parcel.getUuid());

        return mapper.mapToCropReadOnlyDTO(saved, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CropReadOnlyDTO updateCrop(CropUpdateDTO dto, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        Farmer farmer = getFarmer(username);
        Crop existing = getOwnedCrop(dto.uuid(), farmer);
        CropType cropType = getCropType(dto.cropTypeId());

        Crop toUpdate = mapper.mapToCropEntity(dto, existing.getParcel(), cropType);
        toUpdate.setId(existing.getId());

        Crop updated = cropRepository.save(toUpdate);
        log.info("Crop with uuid={} updated", updated.getUuid());

        return mapper.mapToCropReadOnlyDTO(updated, findHarvestDate(updated.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public CropReadOnlyDTO getCrop(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        Crop crop = getOwnedCrop(uuid, getFarmer(username));
        return mapper.mapToCropReadOnlyDTO(crop, findHarvestDate(crop.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Paginated<CropReadOnlyDTO> getCropsFilteredPaginated(CropFilters filters, String username)
            throws AppObjectNotFoundException {

        Farmer farmer = getFarmer(username);
        Page<Crop> page = cropRepository.findAll(buildSpecification(filters, farmer.getId()), filters.getPageable());

        // Μία ερώτηση για όλες τις συγκομιδές της σελίδας, αντί για μία ανά
        // καλλιέργεια.
        Map<Long, LocalDate> harvestDates = findHarvestDates(page.getContent());

        return Paginated.fromPage(page.map(c -> mapper.mapToCropReadOnlyDTO(c, harvestDates.get(c.getId()))));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCrop(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException, AppObjectInvalidArgumentException {

        Crop crop = getOwnedCrop(uuid, getFarmer(username));

        if (!fieldActivityRepository.findByCropIdOrderByActivityDateDesc(crop.getId()).isEmpty()) {
            throw new AppObjectInvalidArgumentException("Crop",
                    "Crop with uuid " + uuid + " has logbook entries and cannot be deleted");
        }

        cropRepository.delete(crop);
        log.info("Crop with uuid={} deleted", uuid);
    }

    private Farmer getFarmer(String username) throws AppObjectNotFoundException {
        return farmerRepository.findByUserUsername(username)
                .orElseThrow(() -> new AppObjectNotFoundException("Farmer",
                        "Farmer for username " + username + " not found"));
    }

    private Parcel getOwnedParcel(String uuid, Farmer farmer)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        Parcel parcel = parcelRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Parcel",
                        "Parcel with uuid " + uuid + " not found"));

        if (!parcel.getFarmer().getId().equals(farmer.getId())) {
            throw new AppObjectNotAuthorizedException("Parcel",
                    "Parcel with uuid " + uuid + " does not belong to the requesting farmer");
        }
        return parcel;
    }

    private Crop getOwnedCrop(String uuid, Farmer farmer)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        Crop crop = cropRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Crop",
                        "Crop with uuid " + uuid + " not found"));

        if (!crop.getParcel().getFarmer().getId().equals(farmer.getId())) {
            throw new AppObjectNotAuthorizedException("Crop",
                    "Crop with uuid " + uuid + " does not belong to the requesting farmer");
        }
        return crop;
    }

    private CropType getCropType(Long id) throws AppObjectNotFoundException {
        return cropTypeRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("CropType",
                        "Crop type with id " + id + " not found"));
    }

    private LocalDate findHarvestDate(Long cropId) {
        return fieldActivityRepository
                .findFirstByCropIdAndTypeOrderByActivityDateDesc(cropId, ActivityType.HARVEST)
                .map(FieldActivity::getActivityDate)
                .orElse(null);
    }

    private Map<Long, LocalDate> findHarvestDates(List<Crop> crops) {
        if (crops.isEmpty()) return Map.of();

        List<Long> ids = crops.stream().map(Crop::getId).toList();
        return fieldActivityRepository.findByCropIdInAndType(ids, ActivityType.HARVEST).stream()
                .collect(Collectors.toMap(a -> a.getCrop().getId(),
                        FieldActivity::getActivityDate,
                        (a, b) -> a));
    }

    private Specification<Crop> buildSpecification(CropFilters filters, Long farmerId) {
        return CropSpecification.cropFarmerIdIs(farmerId)
                .and(CropSpecification.cropStringFieldLike("uuid", filters.getUuid()))
                .and(CropSpecification.cropParcelUuidIs(filters.getParcelUuid()))
                .and(CropSpecification.cropTypeIdIs(filters.getCropTypeId()))
                .and(CropSpecification.cropCultivationYearIs(filters.getCultivationYear()));
    }
}
