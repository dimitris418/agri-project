package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.agriapp.dto.*;
import gr.aueb.cf.agriapp.model.User;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.repository.RoleRepository;
import gr.aueb.cf.agriapp.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FarmerServiceTest {

    private static final String USERNAME = "owner@example.com";
    private static final String OTHER_USERNAME = "other@example.com";
    private static final String PASSWORD = "Agri2026!";

    @Autowired private FarmerService farmerService;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PersistenceContext private EntityManager em;

    @BeforeEach
    void setup() {
        Role role = new Role();
        role.setName("FARMER");
        roleRepository.save(role);
    }

    private FarmerInsertDTO insertDTO(String username, String vat) {
        return FarmerInsertDTO.builder()
                .registryNumber("123456")
                .phone("6912345678")
                .userInsertDTO(UserInsertDTO.builder()
                        .firstname("Δημήτρης")
                        .lastname("Παπαδάκης")
                        .username(username)
                        .password(PASSWORD)
                        .vat(vat)
                        .build())
                .build();
    }

    private FarmerReadOnlyDTO register() throws Exception {
        return farmerService.registerFarmer(insertDTO(USERNAME, "123456789"));
    }

    private FarmerUpdateDTO updateDTO(FarmerReadOnlyDTO farmer, String password) {
        return FarmerUpdateDTO.builder()
                .id(farmer.id())
                .uuid(farmer.uuid())
                .registryNumber("654321")
                .phone("6900000000")
                .isActive(true)
                .userUpdateDTO(UserUpdateDTO.builder()
                        .id(userRepository.findByUsername(USERNAME).orElseThrow().getId())
                        .firstname("Δημήτριος")
                        .lastname("Παπαδάκης")
                        .username(USERNAME)
                        .password(password)
                        .vat("123456789")
                        .build())
                .build();
    }

    private String storedHash() {
        em.flush();
        em.clear();
        return userRepository.findByUsername(USERNAME).orElseThrow().getPassword();
    }

    @Test
    @DisplayName("Η εγγραφή αναθέτει τον ρόλο FARMER και κρυπτογραφεί το συνθηματικό")
    void registrationAssignsTheRoleAndEncodesThePassword() throws Exception {
        FarmerReadOnlyDTO created = register();

        assertEquals("FARMER", created.userReadOnlyDTO().role());

        User user = userRepository.findByUsername(USERNAME).orElseThrow();
        assertNotEquals(PASSWORD, user.getPassword());
        assertTrue(passwordEncoder.matches(PASSWORD, user.getPassword()));
    }

    @Test
    @DisplayName("Διπλότυπο username απορρίπτεται")
    void duplicateUsernameIsRejected() throws Exception {
        register();

        assertThrows(AppObjectAlreadyExists.class,
                () -> farmerService.registerFarmer(insertDTO(USERNAME, "987654321")));
    }

    @Test
    @DisplayName("Διπλότυπο ΑΦΜ απορρίπτεται")
    void duplicateVatIsRejected() throws Exception {
        register();

        assertThrows(AppObjectAlreadyExists.class,
                () -> farmerService.registerFarmer(insertDTO(OTHER_USERNAME, "123456789")));
    }

    @Test
    @DisplayName("Ενημέρωση χωρίς νέο συνθηματικό διατηρεί το υπάρχον hash")
    void updateWithoutPasswordKeepsTheExistingHash() throws Exception {
        FarmerReadOnlyDTO created = register();
        String before = storedHash();

        farmerService.updateFarmer(updateDTO(created, null), USERNAME);

        String after = storedHash();
        assertEquals(before, after);
        assertTrue(passwordEncoder.matches(PASSWORD, after));
    }

    @Test
    @DisplayName("Ενημέρωση με κενό συνθηματικό διατηρεί το υπάρχον hash")
    void updateWithBlankPasswordKeepsTheExistingHash() throws Exception {
        FarmerReadOnlyDTO created = register();
        String before = storedHash();

        farmerService.updateFarmer(updateDTO(created, "   "), USERNAME);

        assertEquals(before, storedHash());
    }

    @Test
    @DisplayName("Ενημέρωση με νέο συνθηματικό αλλάζει το hash")
    void updateWithPasswordChangesTheHash() throws Exception {
        FarmerReadOnlyDTO created = register();
        String before = storedHash();

        farmerService.updateFarmer(updateDTO(created, "NewPass2026!"), USERNAME);

        String after = storedHash();
        assertNotEquals(before, after);
        assertTrue(passwordEncoder.matches("NewPass2026!", after));
    }

    @Test
    @DisplayName("Η ενημέρωση διατηρεί ρόλο και uuid")
    void updateKeepsTheRoleAndTheUuid() throws Exception {
        FarmerReadOnlyDTO created = register();

        FarmerReadOnlyDTO updated = farmerService.updateFarmer(updateDTO(created, null), USERNAME);

        assertEquals(created.uuid(), updated.uuid());
        assertEquals("FARMER", updated.userReadOnlyDTO().role());
        assertEquals("654321", updated.registryNumber());
    }

    @Test
    @DisplayName("Ενημέρωση με ξένο id απορρίπτεται")
    void updateWithSomebodyElsesIdIsRejected() throws Exception {
        FarmerReadOnlyDTO created = register();

        FarmerUpdateDTO tampered = FarmerUpdateDTO.builder()
                .id(created.id() + 999)
                .uuid(created.uuid())
                .registryNumber("654321")
                .phone("6900000000")
                .isActive(true)
                .userUpdateDTO(updateDTO(created, null).userUpdateDTO())
                .build();

        assertThrows(AppObjectNotFoundException.class,
                () -> farmerService.updateFarmer(tampered, USERNAME));
    }

    @Test
    @DisplayName("Το προφίλ ανακτάται από το username")
    void theProfileIsFetchedByUsername() throws Exception {
        register();

        assertEquals(USERNAME, farmerService.getFarmerByUsername(USERNAME).userReadOnlyDTO().username());
    }

    @Test
    @DisplayName("Άγνωστο username δεν βρίσκει αγρότη")
    void unknownUsernameIsNotFound() {
        assertThrows(AppObjectNotFoundException.class,
                () -> farmerService.getFarmerByUsername("nobody@example.com"));
    }
}
