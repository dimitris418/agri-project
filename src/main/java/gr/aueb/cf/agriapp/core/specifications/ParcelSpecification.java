package gr.aueb.cf.agriapp.core.specifications;

import gr.aueb.cf.agriapp.model.Farmer;
import gr.aueb.cf.agriapp.model.Parcel;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

// Utility Class
public class ParcelSpecification {

    private ParcelSpecification() {
    }

    public static Specification<Parcel> parcelFarmerIdIs(Long farmerId) {
        return (root, query, builder) -> {
            Join<Parcel, Farmer> farmer = root.join("farmer");
            return builder.equal(farmer.get("id"), farmerId);
        };
    }

    public static Specification<Parcel> parcelIsActive(Boolean isActive) {
        return (root, query, builder) -> {
            if (isActive == null) return builder.isTrue(builder.literal(true));
            return builder.equal(root.get("isActive"), isActive);
        };
    }

    public static Specification<Parcel> parcelStringFieldLike(String field, String value) {
        return (root, query, builder) -> {
            if (value == null || value.trim().isEmpty()) return builder.isTrue(builder.literal(true));
            return builder.like(builder.upper(root.get(field)), "%" + value.toUpperCase() + "%");
        };
    }
}
