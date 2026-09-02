package gr.aueb.cf.agriapp.core.specifications;

import gr.aueb.cf.agriapp.model.Crop;
import gr.aueb.cf.agriapp.model.Farmer;
import gr.aueb.cf.agriapp.model.Parcel;
import gr.aueb.cf.agriapp.model.static_data.CropType;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

// Utility Class
public class CropSpecification {

    private CropSpecification() {
    }

    /**
     * Ιδιοκτησία μέσω δύο joins: crop -> parcel -> farmer. Χωρίς έλεγχο για
     * null, ώστε να αποτυγχάνει κλειστά.
     */
    public static Specification<Crop> cropFarmerIdIs(Long farmerId) {
        return (root, query, builder) -> {
            Join<Crop, Parcel> parcel = root.join("parcel");
            Join<Parcel, Farmer> farmer = parcel.join("farmer");
            return builder.equal(farmer.get("id"), farmerId);
        };
    }

    public static Specification<Crop> cropParcelUuidIs(String parcelUuid) {
        return (root, query, builder) -> {
            if (parcelUuid == null || parcelUuid.isBlank()) return builder.isTrue(builder.literal(true));
            Join<Crop, Parcel> parcel = root.join("parcel");
            return builder.equal(parcel.get("uuid"), parcelUuid);
        };
    }

    public static Specification<Crop> cropTypeIdIs(Long cropTypeId) {
        return (root, query, builder) -> {
            if (cropTypeId == null) return builder.isTrue(builder.literal(true));
            Join<Crop, CropType> cropType = root.join("cropType");
            return builder.equal(cropType.get("id"), cropTypeId);
        };
    }

    public static Specification<Crop> cropCultivationYearIs(Integer year) {
        return (root, query, builder) -> {
            if (year == null) return builder.isTrue(builder.literal(true));
            return builder.equal(root.get("cultivationYear"), year);
        };
    }

    public static Specification<Crop> cropStringFieldLike(String field, String value) {
        return (root, query, builder) -> {
            if (value == null || value.trim().isEmpty()) return builder.isTrue(builder.literal(true));
            return builder.like(builder.upper(root.get(field)), "%" + value.toUpperCase() + "%");
        };
    }
}
