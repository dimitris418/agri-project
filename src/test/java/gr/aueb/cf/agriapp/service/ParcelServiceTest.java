package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.exceptions.AppObjectAlreadyExists;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotFoundException;
import gr.aueb.cf.agriapp.core.filters.ParcelFilters;
import gr.aueb.cf.agriapp.dto.ParcelInsertDTO;
import gr.aueb.cf.agriapp.dto.ParcelReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.ParcelUpdateDTO;
import gr.aueb.cf.agriapp.model.Farmer;
import gr.aueb.cf.agriapp.model.User;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.repository.FarmerRepository;
import gr.aueb.cf.agriapp.repository.ParcelRepository;
import gr.aueb.cf.agriapp.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ParcelServiceTest {

    private static final String OWNER = "owner@example.com";
    private static final String OTHER = "other@example.com";
    private static final String KAEK = "123456789012";

    @Autowired private ParcelService parcelService;
    @Autowired private RoleRepository roleRepository;
    @Autowired private FarmerRepository farmerRepository;
    @Autowired private ParcelRepository parcelRepository;

    @BeforeEach
    void setup() {
        Role role = new Role();
        role.setName("FARMER");
        roleRepository.save(role);

        createFarmer(OWNER, "111111111", role);
        createFarmer(OTHER, "222222222", role);
    }

    private void createFarmer(String username, String vat, Role role) {
        User user = new User();
        user.setFirstname("Test");
        user.setLastname("Farmer");
        user.setUsername(username);
        user.setPassword("irrelevant");
        user.setVat(vat);
        user.setIsActive(true);
        user.setRole(role);

        Farmer farmer = new Farmer();
        farmer.setUuid(UUID.randomUUID().toString());
        farmer.setIsActive(true);
        farmer.setUser(user);
        farmerRepository.save(farmer);
    }

    private ParcelInsertDTO insertDTO(String name, String kaek) {
        return ParcelInsertDTO.builder()
                .name(name)
                .location("Λάρισα")
                .areaInStremmas(new BigDecimal("25.50"))
                .kaek(kaek)
                .isActive(true)
                .build();
    }

    private ParcelUpdateDTO updateDTO(ParcelReadOnlyDTO parcel, String kaek) {
        return ParcelUpdateDTO.builder()
                .id(parcel.id())
                .uuid(parcel.uuid())
                .name("Ανανεωμένο")
                .location("Καρδίτσα")
                .areaInStremmas(new BigDecimal("30.00"))
                .kaek(kaek)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Το αγροτεμάχιο αποδίδεται στον συνδεδεμένο αγρότη")
    void theParcelIsAssignedToTheAuthenticatedFarmer() throws Exception {
        ParcelReadOnlyDTO created = parcelService.saveParcel(insertDTO("Κάτω χωράφι", KAEK), OWNER);

        Farmer owner = farmerRepository.findByUserUsername(OWNER).orElseThrow();
        assertEquals(owner.getId(),
                parcelRepository.findByUuid(created.uuid()).orElseThrow().getFarmer().getId());
    }

    @Test
    @DisplayName("Το κενό ΚΑΕΚ αποθηκεύεται ως null και δεν εμποδίζει δεύτερη εγγραφή")
    void blankKaekIsStoredAsNullAndDoesNotBlockASecondParcel() throws Exception {
        ParcelReadOnlyDTO first = parcelService.saveParcel(insertDTO("Πρώτο", ""), OWNER);
        assertNull(first.kaek());

        assertDoesNotThrow(() -> parcelService.saveParcel(insertDTO("Δεύτερο", ""), OWNER));
    }

    @Test
    @DisplayName("Το ίδιο ΚΑΕΚ σε δεύτερο αγρότη απορρίπτεται")
    void theSameKaekForAnotherFarmerIsRejected() throws Exception {
        parcelService.saveParcel(insertDTO("Κάτω χωράφι", KAEK), OWNER);

        assertThrows(AppObjectAlreadyExists.class,
                () -> parcelService.saveParcel(insertDTO("Ξένο χωράφι", KAEK), OTHER));
    }

    @Test
    @DisplayName("Η ενημέρωση με το ίδιο ΚΑΕΚ δεν θεωρείται διπλότυπο")
    void updatingWithTheSameKaekIsNotADuplicate() throws Exception {
        ParcelReadOnlyDTO created = parcelService.saveParcel(insertDTO("Κάτω χωράφι", KAEK), OWNER);

        ParcelReadOnlyDTO updated = parcelService.updateParcel(updateDTO(created, KAEK), OWNER);

        assertEquals("Ανανεωμένο", updated.name());
        assertEquals(KAEK, updated.kaek());
    }

    @Test
    @DisplayName("Η ανάγνωση ξένου αγροτεμαχίου απορρίπτεται")
    void readingAnotherFarmersParcelIsRejected() throws Exception {
        ParcelReadOnlyDTO created = parcelService.saveParcel(insertDTO("Κάτω χωράφι", KAEK), OWNER);

        assertThrows(AppObjectNotAuthorizedException.class,
                () -> parcelService.getParcel(created.uuid(), OTHER));
    }

    @Test
    @DisplayName("Η ενημέρωση ξένου αγροτεμαχίου απορρίπτεται")
    void updatingAnotherFarmersParcelIsRejected() throws Exception {
        ParcelReadOnlyDTO created = parcelService.saveParcel(insertDTO("Κάτω χωράφι", KAEK), OWNER);

        assertThrows(AppObjectNotAuthorizedException.class,
                () -> parcelService.updateParcel(updateDTO(created, KAEK), OTHER));
    }

    @Test
    @DisplayName("Η διαγραφή ξένου αγροτεμαχίου απορρίπτεται")
    void deactivatingAnotherFarmersParcelIsRejected() throws Exception {
        ParcelReadOnlyDTO created = parcelService.saveParcel(insertDTO("Κάτω χωράφι", KAEK), OWNER);

        assertThrows(AppObjectNotAuthorizedException.class,
                () -> parcelService.deactivateParcel(created.uuid(), OTHER));
    }

    @Test
    @DisplayName("Η διαγραφή είναι λογική: το αγροτεμάχιο παραμένει ανακτήσιμο ως ανενεργό")
    void deletionIsLogicalAndTheParcelRemains() throws Exception {
        ParcelReadOnlyDTO created = parcelService.saveParcel(insertDTO("Κάτω χωράφι", KAEK), OWNER);

        parcelService.deactivateParcel(created.uuid(), OWNER);

        assertFalse(parcelService.getParcel(created.uuid(), OWNER).isActive());
        assertTrue(parcelRepository.findByUuid(created.uuid()).isPresent());
    }

    @Test
    @DisplayName("Η λίστα περιέχει μόνο τα αγροτεμάχια του αιτούντος")
    void theListContainsOnlyTheRequestersParcels() throws Exception {
        parcelService.saveParcel(insertDTO("Δικό μου", KAEK), OWNER);
        parcelService.saveParcel(insertDTO("Ξένο", "999999999999"), OTHER);

        var mine = parcelService.getParcelsFilteredPaginated(ParcelFilters.builder().build(), OWNER);

        assertEquals(1, mine.getTotalElements());
        assertEquals("Δικό μου", mine.getData().get(0).name());
    }

    @Test
    @DisplayName("Άγνωστο uuid δεν βρίσκεται")
    void unknownUuidIsNotFound() {
        assertThrows(AppObjectNotFoundException.class,
                () -> parcelService.getParcel("does-not-exist", OWNER));
    }
}
