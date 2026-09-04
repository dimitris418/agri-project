package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.agriapp.core.exceptions.AppServerException;
import gr.aueb.cf.agriapp.dto.FarmerInsertDTO;
import gr.aueb.cf.agriapp.dto.FarmerReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.FarmerUpdateDTO;
import gr.aueb.cf.agriapp.mapper.Mapper;
import gr.aueb.cf.agriapp.model.Farmer;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.repository.FarmerRepository;
import gr.aueb.cf.agriapp.repository.RoleRepository;
import gr.aueb.cf.agriapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            throw new AppObjectAlreadyExists("User", "User with username " + username + " already exists");
        }

        if (userRepository.findByVat(vat).isPresent()) {
            throw new AppObjectAlreadyExists("User", "User with vat " + vat + " already exists");
        }

        Role farmerRole = roleRepository.findByName(FARMER_ROLE)
                .orElseThrow(() -> new AppServerException("RoleNotConfigured",
                        "Role " + FARMER_ROLE + " is missing from the database"));

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
                        "Farmer for username " + username + " not found"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FarmerReadOnlyDTO updateFarmer(FarmerUpdateDTO dto, String username)
            throws AppObjectNotFoundException, AppObjectAlreadyExists {

        Farmer existing = farmerRepository.findByUserUsername(username)
                .orElseThrow(() -> new AppObjectNotFoundException("Farmer",
                        "Farmer for username " + username + " not found"));

        // Ο αγρότης ενημερώνει μόνο τον εαυτό του: το id του DTO αγνοείται
        // ως στόχος και χρησιμοποιείται μόνο για έλεγχο συνέπειας.
        if (!existing.getId().equals(dto.id())) {
            throw new AppObjectNotFoundException("Farmer",
                    "Farmer with id " + dto.id() + " not found");
        }

        String newUsername = dto.userUpdateDTO().username();
        if (!existing.getUser().getUsername().equals(newUsername)
                && userRepository.findByUsername(newUsername).isPresent()) {
            throw new AppObjectAlreadyExists("User", "User with username " + newUsername + " already exists");
        }

        String newVat = dto.userUpdateDTO().vat();
        if (!existing.getUser().getVat().equals(newVat)
                && userRepository.findByVat(newVat).isPresent()) {
            throw new AppObjectAlreadyExists("User", "User with vat " + newVat + " already exists");
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
}
