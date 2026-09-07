package gr.aueb.cf.agriapp.repository;

import gr.aueb.cf.agriapp.model.static_data.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
}
