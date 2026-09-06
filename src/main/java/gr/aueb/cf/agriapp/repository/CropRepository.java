package gr.aueb.cf.agriapp.repository;

import gr.aueb.cf.agriapp.model.Crop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long>,
        JpaSpecificationExecutor<Crop> {

    Optional<Crop> findByUuid(String uuid);

    List<Crop> findByParcelId(Long parcelId);

    List<Crop> findByParcelFarmerId(Long farmerId);

    /**
     * Το parcel είναι LAZY, ενώ το ReadOnly DTO το περιλαμβάνει. Χωρίς το
     * entity graph η σελίδα θα εκτελούσε ένα query ανά καλλιέργεια.
     */
    @Override
    @EntityGraph(attributePaths = {"parcel", "cropType"})
    Page<Crop> findAll(Specification<Crop> spec, Pageable pageable);
}
