package gr.aueb.cf.agriapp.core.specifications;

import gr.aueb.cf.agriapp.model.Farmer;
import org.springframework.data.jpa.domain.Specification;

// Utility Class
public class FarmerSpecification {

    private FarmerSpecification() {
    }

    public static Specification<Farmer> farmerIsActive(Boolean isActive) {
        return (root, query, builder) -> {
            if (isActive == null) return builder.isTrue(builder.literal(true));
            return builder.equal(root.get("isActive"), isActive);
        };
    }

    public static Specification<Farmer> farmerStringFieldLike(String field, String value) {
        return (root, query, builder) -> {
            if (value == null || value.trim().isEmpty()) return builder.isTrue(builder.literal(true));
            return builder.like(builder.upper(root.get(field)), "%" + value.toUpperCase() + "%");
        };
    }

    /**
     * Δύο φίλτρα πάνω στον User θα δημιουργούσαν δύο ξεχωριστά joins αν
     * χρησιμοποιούσαμε root.join(). Η διαδρομή root.get("user") αφήνει τον
     * Hibernate να επαναχρησιμοποιήσει το ίδιο join.
     */
    public static Specification<Farmer> farmerUserStringFieldLike(String field, String value) {
        return (root, query, builder) -> {
            if (value == null || value.trim().isEmpty()) return builder.isTrue(builder.literal(true));
            return builder.like(builder.upper(root.get("user").get(field)), "%" + value.toUpperCase() + "%");
        };
    }
}
