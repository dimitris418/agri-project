package gr.aueb.cf.agriapp.core.specifications;

import gr.aueb.cf.agriapp.core.enums.ActivityType;
import gr.aueb.cf.agriapp.model.Crop;
import gr.aueb.cf.agriapp.model.Farmer;
import gr.aueb.cf.agriapp.model.FieldActivity;
import gr.aueb.cf.agriapp.model.Parcel;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

// Utility Class
public class FieldActivitySpecification {

    private FieldActivitySpecification() {
    }

    /**
     * Ιδιοκτησία μέσω τριών joins: activity -> crop -> parcel -> farmer.
     * Αυτή είναι η αλυσίδα που κάνει το μοντέλο να δουλεύει: όσο βαθιά κι αν
     * είναι μια εγγραφή, ο ιδιοκτήτης της βρίσκεται με ένα query.
     */
    public static Specification<FieldActivity> activityFarmerIdIs(Long farmerId) {
        return (root, query, builder) -> {
            Join<FieldActivity, Crop> crop = root.join("crop");
            Join<Crop, Parcel> parcel = crop.join("parcel");
            Join<Parcel, Farmer> farmer = parcel.join("farmer");
            return builder.equal(farmer.get("id"), farmerId);
        };
    }

    public static Specification<FieldActivity> activityCropUuidIs(String cropUuid) {
        return (root, query, builder) -> {
            if (cropUuid == null || cropUuid.isBlank()) return builder.isTrue(builder.literal(true));
            Join<FieldActivity, Crop> crop = root.join("crop");
            return builder.equal(crop.get("uuid"), cropUuid);
        };
    }

    public static Specification<FieldActivity> activityTypeIs(ActivityType type) {
        return (root, query, builder) -> {
            if (type == null) return builder.isTrue(builder.literal(true));
            return builder.equal(root.get("type"), type);
        };
    }

    public static Specification<FieldActivity> activityDateFrom(LocalDate dateFrom) {
        return (root, query, builder) -> {
            if (dateFrom == null) return builder.isTrue(builder.literal(true));
            return builder.greaterThanOrEqualTo(root.get("activityDate"), dateFrom);
        };
    }

    public static Specification<FieldActivity> activityDateTo(LocalDate dateTo) {
        return (root, query, builder) -> {
            if (dateTo == null) return builder.isTrue(builder.literal(true));
            return builder.lessThanOrEqualTo(root.get("activityDate"), dateTo);
        };
    }

    public static Specification<FieldActivity> activityStringFieldLike(String field, String value) {
        return (root, query, builder) -> {
            if (value == null || value.trim().isEmpty()) return builder.isTrue(builder.literal(true));
            return builder.like(builder.upper(root.get(field)), "%" + value.toUpperCase() + "%");
        };
    }
}
