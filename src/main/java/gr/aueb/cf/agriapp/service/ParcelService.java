package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.agriapp.core.filters.Paginated;
import gr.aueb.cf.agriapp.core.filters.ParcelFilters;
import gr.aueb.cf.agriapp.core.specifications.ParcelSpecification;
import gr.aueb.cf.agriapp.dto.ParcelInsertDTO;
import gr.aueb.cf.agriapp.dto.ParcelReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.ParcelUpdateDTO;
import gr.aueb.cf.agriapp.mapper.Mapper;
import gr.aueb.cf.agriapp.model.Farmer;
import gr.aueb.cf.agriapp.model.Parcel;
import gr.aueb.cf.agriapp.repository.FarmerRepository;
import gr.aueb.cf.agriapp.repository.ParcelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParcelService implements IParcelService {

    private final ParcelRepository parcelRepository;
    private final FarmerRepository farmerRepository;
    private final Mapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParcelReadOnlyDTO saveParcel(ParcelInsertDTO dto, String username)
            throws AppObjectNotFoundException, AppObjectAlreadyExists {

        Farmer farmer = getFarmer(username);
        assertKaekIsFree(dto.kaek(), null);

        Parcel saved = parcelRepository.save(mapper.mapToParcelEntity(dto, farmer));
        log.info("Parcel with uuid={} created for username={}", saved.getUuid(), username);

        return mapper.mapToParcelReadOnlyDTO(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParcelReadOnlyDTO updateParcel(ParcelUpdateDTO dto, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException, AppObjectAlreadyExists {

        Farmer farmer = getFarmer(username);
        Parcel existing = getOwnedParcel(dto.uuid(), farmer);

        assertKaekIsFree(dto.kaek(), existing.getId());

        Parcel toUpdate = mapper.mapToParcelEntity(dto, farmer);
        toUpdate.setId(existing.getId());

        Parcel updated = parcelRepository.save(toUpdate);
        log.info("Parcel with uuid={} updated", updated.getUuid());

        return mapper.mapToParcelReadOnlyDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ParcelReadOnlyDTO getParcel(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        return mapper.mapToParcelReadOnlyDTO(getOwnedParcel(uuid, getFarmer(username)));
    }

    @Override
    @Transactional(readOnly = true)
    public Paginated<ParcelReadOnlyDTO> getParcelsFilteredPaginated(ParcelFilters filters, String username)
            throws AppObjectNotFoundException {

        Farmer farmer = getFarmer(username);
        var filtered = parcelRepository.findAll(buildSpecification(filters, farmer.getId()), filters.getPageable());

        return Paginated.fromPage(filtered.map(mapper::mapToParcelReadOnlyDTO));
    }

    /**
     * Λογική διαγραφή. Το ημερολόγιο αγρού είναι αρχείο καταγραφής -- η
     * φυσική διαγραφή αγροτεμαχίου θα παρέσυρε καλλιέργειες και εργασίες
     * που πρέπει να παραμείνουν ανακτήσιμες.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivateParcel(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        Parcel parcel = getOwnedParcel(uuid, getFarmer(username));
        parcel.setIsActive(false);
        parcelRepository.save(parcel);
        log.info("Parcel with uuid={} deactivated", uuid);
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

    private void assertKaekIsFree(String kaek, Long currentParcelId) throws AppObjectAlreadyExists {
        if (kaek == null || kaek.isBlank()) return;

        var owner = parcelRepository.findByKaek(kaek);
        if (owner.isPresent() && !owner.get().getId().equals(currentParcelId)) {
            throw new AppObjectAlreadyExists("Parcel", "Parcel with kaek " + kaek + " already exists");
        }
    }

    private Specification<Parcel> buildSpecification(ParcelFilters filters, Long farmerId) {
        return ParcelSpecification.parcelFarmerIdIs(farmerId)
                .and(ParcelSpecification.parcelStringFieldLike("uuid", filters.getUuid()))
                .and(ParcelSpecification.parcelStringFieldLike("name", filters.getName()))
                .and(ParcelSpecification.parcelStringFieldLike("kaek", filters.getKaek()))
                .and(ParcelSpecification.parcelIsActive(filters.getActive()));
    }
}
