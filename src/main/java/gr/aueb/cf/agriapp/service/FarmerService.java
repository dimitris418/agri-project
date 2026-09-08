package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.agriapp.core.exceptions.AppServerException;
import gr.aueb.cf.agriapp.core.filters.FarmerFilters;
import gr.aueb.cf.agriapp.core.filters.Paginated;
import gr.aueb.cf.agriapp.core.specifications.FarmerSpecification;
import gr.aueb.cf.agriapp.dto.FarmerInsertDTO;
import gr.aueb.cf.agriapp.dto.FarmerReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.FarmerStatusUpdateDTO;
import gr.aueb.cf.agriapp.dto.FarmerUpdateDTO;
import gr.aueb.cf.agriapp.mapper.Mapper;
import gr.aueb.cf.agriapp.model.Farmer;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.repository.FarmerRepository;
import gr.aueb.cf.agriapp.repository.RoleRepository;
import gr.aueb.cf.agriapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FarmerService implements IFarmerService {

    private static final String FARMER_ROLE = "FARMER";

    private final FarmerRepository farmerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Mapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FarmerReadOnlyDTO registerFarmer(FarmerInsertDTO dto)
            throws AppObjectAlreadyExists, AppServerException {

        String username = dto.userInsertDTO().username();
        String vat = dto.userInsertDTO().vat();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new AppObjectAlreadyExists("User", "Υπάρχει ήδη χρήστης με όνομα " + username);
        }

        if (userRepository.findByVat(vat).isPresent()) {
            throw new AppObjectAlreadyExists("User", "Υπάρχει ήδη χρήστης με ΑΦΜ " + vat);
        }

        Role farmerRole = roleRepository.findByName(FARMER_ROLE)
                .orElseThrow(() -> new AppServerException("RoleNotConfigured",
                        "Ο ρόλος " + FARMER_ROLE + " λείπει από τη βάση"));

        Farmer farmer = mapper.mapToFarmerEntity(dto);
        farmer.getUser().setRole(farmerRole);

        Farmer saved = farmerRepository.save(farmer);
        log.info("Farmer with username={} registered", username);

        return mapper.mapToFarmerReadOnlyDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FarmerReadOnlyDTO getFarmerByUsername(String username)
            throws AppObjectNotFoundException {

        return farmerRepository.findByUserUsername(username)
                .map(mapper::mapToFarmerReadOnlyDTO)
                .orElseThrow(() -> new AppObjectNotFoundException("Farmer",
                        "Δεν βρέθηκε αγρότης για τον χρήστη " + username));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FarmerReadOnlyDTO updateFarmer(FarmerUpdateDTO dto, String username)
            throws AppObjectNotFoundException, AppObjectAlreadyExists {

        Farmer existing = farmerRepository.findByUserUsername(username)
                .orElseThrow(() -> new AppObjectNotFoundException("Farmer",
                        "Δεν βρέθηκε αγρότης για τον χρήστη " + username));

        // Ο αγρότης ενημερώνει μόνο τον εαυτό του: το id του DTO αγνοείται
        // ως στόχος και χρησιμοποιείται μόνο για έλεγχο συνέπειας.
        if (!existing.getId().equals(dto.id())) {
            throw new AppObjectNotFoundException("Farmer",
                    "Δεν βρέθηκε αγρότης με id " + dto.id());
        }

        String newUsername = dto.userUpdateDTO().username();
        if (!existing.getUser().getUsername().equals(newUsername)
                && userRepository.findByUsername(newUsername).isPresent()) {
            throw new AppObjectAlreadyExists("User", "Υπάρχει ήδη χρήστης με όνομα " + newUsername);
        }

        String newVat = dto.userUpdateDTO().vat();
        if (!existing.getUser().getVat().equals(newVat)
                && userRepository.findByVat(newVat).isPresent()) {
            throw new AppObjectAlreadyExists("User", "Υπάρχει ήδη χρήστης με ΑΦΜ " + newVat);
        }

        Farmer toUpdate = mapper.mapToFarmerEntity(dto);
        toUpdate.setUuid(existing.getUuid());
        toUpdate.getUser().setId(existing.getUser().getId());
        toUpdate.getUser().setRole(existing.getUser().getRole());

        // Το mapper αφήνει το password null όταν δεν στάλθηκε. Χωρίς αυτό,
        // το merge θα έγραφε NULL και ο χρήστης δεν θα ξανασυνδεόταν ποτέ.
        if (toUpdate.getUser().getPassword() == null) {
            toUpdate.getUser().setPassword(existing.getUser().getPassword());
        }

        Farmer updated = farmerRepository.save(toUpdate);
        log.info("Farmer with username={} updated", username);

        return mapper.mapToFarmerReadOnlyDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Paginated<FarmerReadOnlyDTO> getFarmersFilteredPaginated(FarmerFilters filters) {
        var filtered = farmerRepository.findAll(getSpecsFromFilters(filters), filters.getPageable());
        return Paginated.fromPage(filtered.map(mapper::mapToFarmerReadOnlyDTO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FarmerReadOnlyDTO setFarmerStatus(String uuid, FarmerStatusUpdateDTO dto)
            throws AppObjectNotFoundException {

        Farmer farmer = farmerRepository.findByUuid(uuid)
                .orElseThrow(() -> new AppObjectNotFoundException("Farmer",
                        "Δεν βρέθηκε αγρότης με uuid " + uuid));

        // Και οι δύο σημαίες: η του Farmer κρύβει τον αγρότη από τις λίστες,
        // η του User είναι αυτή που το isEnabled() διαβάζει για τη σύνδεση.
        farmer.setIsActive(dto.isActive());
        farmer.getUser().setIsActive(dto.isActive());

        Farmer saved = farmerRepository.save(farmer);
        log.info("Farmer with uuid={} set to isActive={}", uuid, dto.isActive());

        return mapper.mapToFarmerReadOnlyDTO(saved);
    }

    private Specification<Farmer> getSpecsFromFilters(FarmerFilters filters) {
        return FarmerSpecification.farmerStringFieldLike("uuid", filters.getUuid())
                .and(FarmerSpecification.farmerStringFieldLike("registryNumber", filters.getRegistryNumber()))
                .and(FarmerSpecification.farmerUserStringFieldLike("lastname", filters.getLastname()))
                .and(FarmerSpecification.farmerUserStringFieldLike("username", filters.getUsername()))
                .and(FarmerSpecification.farmerIsActive(filters.getActive()));
    }
}
