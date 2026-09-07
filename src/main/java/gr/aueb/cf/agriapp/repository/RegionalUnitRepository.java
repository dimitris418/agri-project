package gr.aueb.cf.agriapp.repository;

import gr.aueb.cf.agriapp.model.static_data.RegionalUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionalUnitRepository extends JpaRepository<RegionalUnit, Long> {

    List<RegionalUnit> findAllByOrderByRegionNameAscNameAsc();
}
