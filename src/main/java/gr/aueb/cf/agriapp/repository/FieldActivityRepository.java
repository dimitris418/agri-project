package gr.aueb.cf.agriapp.repository;

import gr.aueb.cf.agriapp.core.enums.ActivityType;
import gr.aueb.cf.agriapp.model.FieldActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FieldActivityRepository extends JpaRepository<FieldActivity, Long>,
        JpaSpecificationExecutor<FieldActivity> {

    Optional<FieldActivity> findByUuid(String uuid);

    List<FieldActivity> findByCropIdOrderByActivityDateDesc(Long cropId);

    List<FieldActivity> findByCropIdInAndType(Collection<Long> cropIds, ActivityType type);

    /**
     * Η πιο πρόσφατη εργασία ενός τύπου για μια καλλιέργεια. Χρησιμοποιείται
     * για τον έλεγχο του χρόνου αναμονής (τελευταίος ψεκασμός) και για να
     * βρεθεί η πραγματική συγκομιδή, που δεν αποθηκεύεται στο Crop.
     */
    Optional<FieldActivity> findFirstByCropIdAndTypeOrderByActivityDateDesc(
            Long cropId, ActivityType type);

    /**
     * Το ReadOnly DTO περιλαμβάνει καλλιέργεια, σκεύασμα και εχθρό, που είναι
     * όλα LAZY. Χωρίς το entity graph κάθε γραμμή της σελίδας θα εκτελούσε
     * δικά της queries.
     */
    @Override
    @EntityGraph(attributePaths = {"crop", "crop.parcel", "crop.cropType", "product", "pest"})
    Page<FieldActivity> findAll(Specification<FieldActivity> spec, Pageable pageable);
}
