package gr.aueb.cf.agriapp.repository;

import gr.aueb.cf.agriapp.core.enums.CropSeason;
import gr.aueb.cf.agriapp.model.static_data.CropType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropTypeRepository extends JpaRepository<CropType, Long> {

    Optional<CropType> findByName(String name);

    List<CropType> findBySeason(CropSeason season);
}
