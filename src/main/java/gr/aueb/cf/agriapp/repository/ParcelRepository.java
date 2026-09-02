package gr.aueb.cf.agriapp.repository;

import gr.aueb.cf.agriapp.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long>,
        JpaSpecificationExecutor<Parcel> {

    Optional<Parcel> findByUuid(String uuid);

    Optional<Parcel> findByKaek(String kaek);

    List<Parcel> findByFarmerId(Long farmerId);
}
