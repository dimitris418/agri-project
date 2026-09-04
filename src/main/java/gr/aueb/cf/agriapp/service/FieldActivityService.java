package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.enums.ActivityType;
import gr.aueb.cf.agriapp.core.enums.ProductCategory;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.agriapp.core.filters.FieldActivityFilters;
import gr.aueb.cf.agriapp.core.filters.Paginated;
import gr.aueb.cf.agriapp.core.specifications.FieldActivitySpecification;
import gr.aueb.cf.agriapp.dto.FieldActivityInsertDTO;
import gr.aueb.cf.agriapp.dto.FieldActivityReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.FieldActivityUpdateDTO;
import gr.aueb.cf.agriapp.mapper.Mapper;
import gr.aueb.cf.agriapp.model.Crop;
import gr.aueb.cf.agriapp.model.Farmer;
import gr.aueb.cf.agriapp.model.FieldActivity;
import gr.aueb.cf.agriapp.model.static_data.Pest;
import gr.aueb.cf.agriapp.model.static_data.Product;
import gr.aueb.cf.agriapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FieldActivityService implements IFieldActivityService {

    private final FieldActivityRepository fieldActivityRepository;
    private final CropRepository cropRepository;
    private final ProductRepository productRepository;
    private final PestRepository pestRepository;
    private final FarmerRepository farmerRepository;
    private final Mapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FieldActivityReadOnlyDTO saveActivity(FieldActivityInsertDTO dto, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException, AppObjectInvalidArgumentException {

        Crop crop = getOwnedCrop(dto.cropUuid(), getFarmer(username));
        Product product = findProduct(dto.productId());
        Pest pest = findPest(dto.pestId());

        FieldActivity candidate = mapper.mapToFieldActivityEntity(dto, crop, product, pest);

        assertFieldsRequiredByType(candidate);
        assertLogbookRules(crop, candidate);

        FieldActivity saved = fieldActivityRepository.save(candidate);
        log.info("Activity type={} recorded on crop={}", saved.getType(), crop.getUuid());

        return mapper.mapToFieldActivityReadOnlyDTO(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FieldActivityReadOnlyDTO updateActivity(FieldActivityUpdateDTO dto, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException, AppObjectInvalidArgumentException {

        Farmer farmer = getFarmer(username);
        FieldActivity existing = getOwnedActivity(dto.uuid(), farmer);
        Product product = findProduct(dto.productId());
        Pest pest = findPest(dto.pestId());

        FieldActivity candidate = mapper.mapToFieldActivityEntity(dto, existing.getCrop(), product, pest);
        candidate.setId(existing.getId());

        assertFieldsRequiredByType(candidate);
        assertLogbookRules(existing.getCrop(), candidate);

        FieldActivity updated = fieldActivityRepository.save(candidate);
        log.info("Activity with uuid={} updated", updated.getUuid());

        return mapper.mapToFieldActivityReadOnlyDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public FieldActivityReadOnlyDTO getActivity(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        return mapper.mapToFieldActivityReadOnlyDTO(getOwnedActivity(uuid, getFarmer(username)));
    }

    @Override
    @Transactional(readOnly = true)
    public Paginated<FieldActivityReadOnlyDTO> getActivitiesFilteredPaginated(FieldActivityFilters filters, String username)
            throws AppObjectNotFoundException {

        Farmer farmer = getFarmer(username);
        var filtered = fieldActivityRepository.findAll(buildSpecification(filters, farmer.getId()), filters.getPageable());

        return Paginated.fromPage(filtered.map(mapper::mapToFieldActivityReadOnlyDTO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(String uuid, String username)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        FieldActivity activity = getOwnedActivity(uuid, getFarmer(username));
        fieldActivityRepository.delete(activity);
        log.info("Activity with uuid={} deleted", uuid);
    }

    private void assertFieldsRequiredByType(FieldActivity a) throws AppObjectInvalidArgumentException {
        switch (a.getType()) {
            case SPRAYING -> {
                requireProduct(a, ProductCategory.HERBICIDE, ProductCategory.FUNGICIDE, ProductCategory.INSECTICIDE);
                requireQuantity(a);
            }
            case FERTILIZATION -> {
                requireProduct(a, ProductCategory.FERTILIZER);
                requireQuantity(a);
            }
            case IRRIGATION, HARVEST -> requireQuantity(a);
            case OBSERVATION -> {
                if (a.getPest() == null) {
                    throw new AppObjectInvalidArgumentException("Activity", "An observation requires a pest");
                }
                if (a.getSeverity() == null) {
                    throw new AppObjectInvalidArgumentException("Activity", "An observation requires a severity level");
                }
            }
        }
    }

    private void requireProduct(FieldActivity a, ProductCategory... allowed) throws AppObjectInvalidArgumentException {
        if (a.getProduct() == null) {
            throw new AppObjectInvalidArgumentException("Activity",
                    "Activity of type " + a.getType() + " requires a product");
        }
        for (ProductCategory c : allowed) {
            if (a.getProduct().getCategory() == c) return;
        }
        throw new AppObjectInvalidArgumentException("Activity",
                "Product " + a.getProduct().getName() + " cannot be used for an activity of type " + a.getType());
    }

    private void requireQuantity(FieldActivity a) throws AppObjectInvalidArgumentException {
        if (a.getQuantity() == null || a.getUnit() == null) {
            throw new AppObjectInvalidArgumentException("Activity",
                    "Activity of type " + a.getType() + " requires a quantity and a unit");
        }
    }

    /**
     * Οι δύο κανόνες του ημερολογίου, ελεγμένοι πάνω στο σύνολο των εγγραφών
     * όπως θα διαμορφωθεί μετά την πράξη -- όχι μόνο πάνω στη νέα εγγραφή.
     */
    private void assertLogbookRules(Crop crop, FieldActivity candidate) throws AppObjectInvalidArgumentException {

        List<FieldActivity> effective = new ArrayList<>(
                fieldActivityRepository.findByCropIdOrderByActivityDateDesc(crop.getId()));

        if (candidate.getId() != null) {
            effective.removeIf(a -> a.getId().equals(candidate.getId()));
        }
        effective.add(candidate);

        List<FieldActivity> harvests = effective.stream()
                .filter(a -> a.getType() == ActivityType.HARVEST)
                .toList();

        if (harvests.size() > 1) {
            throw new AppObjectInvalidArgumentException("Activity", "The crop already has a harvest recorded");
        }
        if (harvests.isEmpty()) return;

        LocalDate harvestDate = harvests.get(0).getActivityDate();

        for (FieldActivity a : effective) {
            if (a.getType() != ActivityType.HARVEST && a.getActivityDate().isAfter(harvestDate)) {
                throw new AppObjectInvalidArgumentException("Activity",
                        "No activity can be dated after the harvest on " + harvestDate);
            }
        }

        // Δεσμευτικός είναι ο ψεκασμός με τη μεγαλύτερη ημερομηνία λήξης
        // αναμονής, που δεν είναι απαραίτητα ο πιο πρόσφατος.
        for (FieldActivity a : effective) {
            if (a.getType() != ActivityType.SPRAYING || a.getProduct() == null) continue;

            Integer phi = a.getProduct().getPreHarvestIntervalDays();
            if (phi == null) continue;

            LocalDate earliestHarvest = a.getActivityDate().plusDays(phi);
            if (earliestHarvest.isAfter(harvestDate)) {
                throw new AppObjectInvalidArgumentException("Activity",
                        "The pre-harvest interval of " + a.getProduct().getName() + " (" + phi
                                + " days) is not respected: harvest cannot precede " + earliestHarvest);
            }
        }
    }

    private Farmer getFarmer(String username) throws AppObjectNotFoundException {
        return farmerRepository.findByUserUsername(username)
                .orElseThrow(() -> new AppObjectNotFoundException("Farmer",
                        "Farmer for username " + username + " not found"));
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

    private FieldActivity getOwnedActivity(String uuid, Farmer farmer)
            throws AppObjectNotFoundException, AppObjectNotAuthorizedException {

        FieldActivity activity = fieldActivityRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Activity",
                        "Activity with uuid " + uuid + " not found"));

        if (!activity.getCrop().getParcel().getFarmer().getId().equals(farmer.getId())) {
            throw new AppObjectNotAuthorizedException("Activity",
                    "Activity with uuid " + uuid + " does not belong to the requesting farmer");
        }
        return activity;
    }

    private Product findProduct(Long id) throws AppObjectNotFoundException {
        if (id == null) return null;
        return productRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Product",
                        "Product with id " + id + " not found"));
    }

    private Pest findPest(Long id) throws AppObjectNotFoundException {
        if (id == null) return null;
        return pestRepository.findById(id)
                .orElseThrow(() -> new AppObjectNotFoundException("Pest",
                        "Pest with id " + id + " not found"));
    }

    private Specification<FieldActivity> buildSpecification(FieldActivityFilters f, Long farmerId) {
        return FieldActivitySpecification.activityFarmerIdIs(farmerId)
                .and(FieldActivitySpecification.activityStringFieldLike("uuid", f.getUuid()))
                .and(FieldActivitySpecification.activityCropUuidIs(f.getCropUuid()))
                .and(FieldActivitySpecification.activityTypeIs(f.getType()))
                .and(FieldActivitySpecification.activityDateFrom(f.getDateFrom()))
                .and(FieldActivitySpecification.activityDateTo(f.getDateTo()));
    }
}
