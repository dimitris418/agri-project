package gr.aueb.cf.agriapp.repository;

import gr.aueb.cf.agriapp.core.enums.PestType;
import gr.aueb.cf.agriapp.model.static_data.Pest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PestRepository extends JpaRepository<Pest, Long> {

    Optional<Pest> findByName(String name);

    List<Pest> findByType(PestType type);
}
