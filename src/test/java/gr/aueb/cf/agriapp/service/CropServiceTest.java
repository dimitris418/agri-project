package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.enums.ActivityType;
import gr.aueb.cf.agriapp.core.enums.CropSeason;
import gr.aueb.cf.agriapp.core.enums.UnitOfMeasure;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.agriapp.core.filters.CropFilters;
import gr.aueb.cf.agriapp.dto.CropInsertDTO;
import gr.aueb.cf.agriapp.dto.CropReadOnlyDTO;
import gr.aueb.cf.agriapp.dto.CropUpdateDTO;
import gr.aueb.cf.agriapp.model.*;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.model.static_data.CropType;
import gr.aueb.cf.agriapp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CropServiceTest {

    private static final String OWNER = "owner@example.com";
    private static final String OTHER = "other@example.com";
    private static final LocalDate HARVEST_DATE = LocalDate.of(2026, 6, 20);

    @Autowired private CropService cropService;
    @Autowired private RoleRepository roleRepository;
    @Autowired private FarmerRepository farmerRepository;
    @Autowired private ParcelRepository parcelRepository;
    @Autowired private CropRepository cropRepository;
    @Autowired private CropTypeRepository cropTypeRepository;
    @Autowired private FieldActivityRepository fieldActivityRepository;

    private Parcel ownParcel;
    private Parcel otherParcel;
    private CropType cropType;

    @BeforeEach
    void setup() {
        Role role = new Role();
        role.setName("FARMER");
        roleRepository.save(role);

        cropType = new CropType();
        cropType.setName("Σκληρό σιτάρι");
        cropType.setLatinName("Triticum durum");
        cropType.setSeason(CropSeason.WINTER);
        cropTypeRepository.save(cropType);

        ownParcel = createParcelFor(OWNER, "111111111", role);
        otherParcel = createParcelFor(OTHER, "222222222", role);
    }

    private Parcel createParcelFor(String username, String vat, Role role) {
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

        Parcel parcel = new Parcel();
        parcel.setUuid(UUID.randomUUID().toString());
        parcel.setName("Χωράφι " + username);
        parcel.setAreaInStremmas(new BigDecimal("10.00"));
        parcel.setIsActive(true);
        parcel.setFarmer(farmer);
        return parcelRepository.save(parcel);
    }

    private CropInsertDTO insertDTO(String parcelUuid) {
        return CropInsertDTO.builder()
                .parcelUuid(parcelUuid)
                .cropTypeId(cropType.getId())
                .variety("Σίμετο")
                .cultivationYear(2026)
                .plantingDate(LocalDate.of(2025, 11, 10))
                .expectedHarvestDate(HARVEST_DATE)
                .build();
    }

    private void recordHarvest(String cropUuid) {
        Crop crop = cropRepository.findByUuid(cropUuid).orElseThrow();

        FieldActivity harvest = new FieldActivity();
        harvest.setUuid(UUID.randomUUID().toString());
        harvest.setActivityDate(HARVEST_DATE);
        harvest.setType(ActivityType.HARVEST);
        harvest.setQuantity(new BigDecimal("8000"));
        harvest.setUnit(UnitOfMeasure.KILOGRAM);
        harvest.setCrop(crop);
        fieldActivityRepository.save(harvest);
    }

    @Test
    @DisplayName("Η καλλιέργεια δημιουργείται σε δικό μας αγροτεμάχιο")
    void aCropIsCreatedOnOurOwnParcel() throws Exception {
        CropReadOnlyDTO created = cropService.saveCrop(insertDTO(ownParcel.getUuid()), OWNER);

        assertNotNull(created.uuid());
        assertEquals("Σκληρό σιτάρι", created.cropTypeReadOnlyDTO().name());
        assertEquals("WINTER", created.cropTypeReadOnlyDTO().season());
    }

    @Test
    @DisplayName("Η δημιουργία σε ξένο αγροτεμάχιο απορρίπτεται")
    void creatingOnAnotherFarmersParcelIsRejected() {
        assertThrows(AppObjectNotAuthorizedException.class,
                () -> cropService.saveCrop(insertDTO(otherParcel.getUuid()), OWNER));
    }

    @Test
    @DisplayName("Χωρίς συγκομιδή το harvestDate είναι null")
    void withoutAHarvestTheDateIsNull() throws Exception {
        CropReadOnlyDTO created = cropService.saveCrop(insertDTO(ownParcel.getUuid()), OWNER);

        assertNull(cropService.getCrop(created.uuid(), OWNER).harvestDate());
    }

    @Test
    @DisplayName("Το harvestDate προκύπτει από την εργασία συγκομιδής")
    void theHarvestDateIsDerivedFromTheActivity() throws Exception {
        CropReadOnlyDTO created = cropService.saveCrop(insertDTO(ownParcel.getUuid()), OWNER);
        recordHarvest(created.uuid());

        assertEquals(HARVEST_DATE, cropService.getCrop(created.uuid(), OWNER).harvestDate());
    }

    @Test
    @DisplayName("Το harvestDate προκύπτει και στη σελιδοποιημένη λίστα")
    void theHarvestDateIsAlsoDerivedInTheList() throws Exception {
        CropReadOnlyDTO created = cropService.saveCrop(insertDTO(ownParcel.getUuid()), OWNER);
        recordHarvest(created.uuid());

        var page = cropService.getCropsFilteredPaginated(CropFilters.builder().build(), OWNER);

        assertEquals(1, page.getTotalElements());
        assertEquals(HARVEST_DATE, page.getData().get(0).harvestDate());
    }

    @Test
    @DisplayName("Καλλιέργεια με εγγραφές ημερολογίου δεν διαγράφεται")
    void aCropWithLogbookEntriesCannotBeDeleted() throws Exception {
        CropReadOnlyDTO created = cropService.saveCrop(insertDTO(ownParcel.getUuid()), OWNER);
        recordHarvest(created.uuid());

        assertThrows(AppObjectInvalidArgumentException.class,
                () -> cropService.deleteCrop(created.uuid(), OWNER));
    }

    @Test
    @DisplayName("Κενή καλλιέργεια διαγράφεται")
    void anEmptyCropIsDeleted() throws Exception {
        CropReadOnlyDTO created = cropService.saveCrop(insertDTO(ownParcel.getUuid()), OWNER);

        cropService.deleteCrop(created.uuid(), OWNER);

        assertTrue(cropRepository.findByUuid(created.uuid()).isEmpty());
    }

    @Test
    @DisplayName("Η διαγραφή ξένης καλλιέργειας απορρίπτεται")
    void deletingAnotherFarmersCropIsRejected() throws Exception {
        CropReadOnlyDTO created = cropService.saveCrop(insertDTO(ownParcel.getUuid()), OWNER);

        assertThrows(AppObjectNotAuthorizedException.class,
                () -> cropService.deleteCrop(created.uuid(), OTHER));
    }

    @Test
    @DisplayName("Η ενημέρωση διατηρεί το αγροτεμάχιο")
    void theUpdateKeepsTheParcel() throws Exception {
        CropReadOnlyDTO created = cropService.saveCrop(insertDTO(ownParcel.getUuid()), OWNER);

        CropUpdateDTO dto = CropUpdateDTO.builder()
                .id(created.id())
                .uuid(created.uuid())
                .cropTypeId(cropType.getId())
                .variety("Μεξικάλι")
                .cultivationYear(2026)
                .plantingDate(LocalDate.of(2025, 11, 15))
                .expectedHarvestDate(HARVEST_DATE)
                .build();

        CropReadOnlyDTO updated = cropService.updateCrop(dto, OWNER);

        assertEquals("Μεξικάλι", updated.variety());
        assertEquals(ownParcel.getId(),
                cropRepository.findByUuid(updated.uuid()).orElseThrow().getParcel().getId());
    }

    @Test
    @DisplayName("Η λίστα περιέχει μόνο τις δικές μας καλλιέργειες")
    void theListContainsOnlyOurOwnCrops() throws Exception {
        cropService.saveCrop(insertDTO(ownParcel.getUuid()), OWNER);
        cropService.saveCrop(insertDTO(otherParcel.getUuid()), OTHER);

        assertEquals(1, cropService
                .getCropsFilteredPaginated(CropFilters.builder().build(), OWNER).getTotalElements());
    }
}
