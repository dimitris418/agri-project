package gr.aueb.cf.agriapp.repository;

import gr.aueb.cf.agriapp.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FarmerRepository extends JpaRepository<Farmer, Long>,
        JpaSpecificationExecutor<Farmer> {

    Optional<Farmer> findByUuid(String uuid);

    Optional<Farmer> findByUserId(Long userId);

    Optional<Farmer> findByUserUsername(String username);
}
