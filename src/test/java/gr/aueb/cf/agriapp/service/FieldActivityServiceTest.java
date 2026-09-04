package gr.aueb.cf.agriapp.service;

import gr.aueb.cf.agriapp.core.enums.*;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.agriapp.core.exceptions.AppObjectNotAuthorizedException;
import gr.aueb.cf.agriapp.dto.FieldActivityInsertDTO;
import gr.aueb.cf.agriapp.model.*;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.model.static_data.CropType;
import gr.aueb.cf.agriapp.model.static_data.Pest;
import gr.aueb.cf.agriapp.model.static_data.Product;
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
class FieldActivityServiceTest {

    private static final String OWNER = "owner@example.com";
    private static final String INTRUDER = "intruder@example.com";
    private static final LocalDate SPRAY_DATE = LocalDate.of(2026, 5, 1);
    private static final int PHI_DAYS = 35;

    @Autowired private FieldActivityService fieldActivityService;
    @Autowired private RoleRepository roleRepository;
    @Autowired private FarmerRepository farmerRepository;
    @Autowired private ParcelRepository parcelRepository;
    @Autowired private CropRepository cropRepository;
    @Autowired private CropTypeRepository cropTypeRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private PestRepository pestRepository;

    private Crop crop;
    private Product fungicide;
    private Product fertilizer;
    private Pest pest;

    @BeforeEach
    void setup() {
        Role role = new Role();
        role.setName("FARMER");
        roleRepository.save(role);

        CropType cropType = new CropType();
        cropType.setName("Σκληρό σιτάρι");
        cropType.setLatinName("Triticum durum");
        cropType.setSeason(CropSeason.WINTER);
        cropTypeRepository.save(cropType);

        fungicide = new Product();
        fungicide.setName("Μυκητοκτόνο δοκιμής");
        fungicide.setActiveSubstance("tebuconazole");
        fungicide.setCategory(ProductCategory.FUNGICIDE);
        fungicide.setPreHarvestIntervalDays(PHI_DAYS);
        productRepository.save(fungicide);

        fertilizer = new Product();
        fertilizer.setName("Λίπασμα δοκιμής");
        fertilizer.setActiveSubstance("N 46%");
        fertilizer.setCategory(ProductCategory.FERTILIZER);
        productRepository.save(fertilizer);

        pest = new Pest();
        pest.setName("Σκωρίαση δοκιμής");
        pest.setLatinName("Puccinia sp.");
        pest.setType(PestType.FUNGAL_DISEASE);
        pestRepository.save(pest);

        crop = createCropFor(OWNER, role, cropType);
        createCropFor(INTRUDER, role, cropType);
    }

    private Crop createCropFor(String username, Role role, CropType cropType) {
        User user = new User();
        user.setFirstname("Test");
        user.setLastname("Farmer");
        user.setUsername(username);
        user.setPassword("irrelevant");
        user.setVat(String.valueOf(100000000 + username.hashCode() % 100000));
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
        parcelRepository.save(parcel);

        Crop c = new Crop();
        c.setUuid(UUID.randomUUID().toString());
        c.setCropType(cropType);
        c.setCultivationYear(2026);
        c.setParcel(parcel);
        return cropRepository.save(c);
    }

    private FieldActivityInsertDTO activity(ActivityType type, LocalDate date) {
        return FieldActivityInsertDTO.builder()
                .cropUuid(crop.getUuid())
                .activityDate(date)
                .type(type)
                .build();
    }

    private void spray(LocalDate date) throws Exception {
        fieldActivityService.saveActivity(FieldActivityInsertDTO.builder()
                .cropUuid(crop.getUuid())
                .activityDate(date)
                .type(ActivityType.SPRAYING)
                .productId(fungicide.getId())
                .quantity(new BigDecimal("1.5"))
                .unit(UnitOfMeasure.LITRE)
                .build(), OWNER);
    }

    private FieldActivityInsertDTO harvest(LocalDate date) {
        return FieldActivityInsertDTO.builder()
                .cropUuid(crop.getUuid())
                .activityDate(date)
                .type(ActivityType.HARVEST)
                .quantity(new BigDecimal("8000"))
                .unit(UnitOfMeasure.KILOGRAM)
                .build();
    }

    @Test
    @DisplayName("Ο ψεκασμός χωρίς σκεύασμα απορρίπτεται")
    void sprayingWithoutProductIsRejected() {
        assertThrows(AppObjectInvalidArgumentException.class,
                () -> fieldActivityService.saveActivity(activity(ActivityType.SPRAYING, SPRAY_DATE), OWNER));
    }

    @Test
    @DisplayName("Ο ψεκασμός με λίπασμα απορρίπτεται")
    void sprayingWithFertilizerIsRejected() {
        FieldActivityInsertDTO dto = FieldActivityInsertDTO.builder()
                .cropUuid(crop.getUuid())
                .activityDate(SPRAY_DATE)
                .type(ActivityType.SPRAYING)
                .productId(fertilizer.getId())
                .quantity(new BigDecimal("1"))
                .unit(UnitOfMeasure.LITRE)
                .build();

        assertThrows(AppObjectInvalidArgumentException.class,
                () -> fieldActivityService.saveActivity(dto, OWNER));
    }

    @Test
    @DisplayName("Η παρατήρηση χωρίς εχθρό απορρίπτεται")
    void observationWithoutPestIsRejected() {
        assertThrows(AppObjectInvalidArgumentException.class,
                () -> fieldActivityService.saveActivity(activity(ActivityType.OBSERVATION, SPRAY_DATE), OWNER));
    }

    @Test
    @DisplayName("Η παρατήρηση με εχθρό και ένταση γίνεται δεκτή")
    void validObservationIsAccepted() throws Exception {
        FieldActivityInsertDTO dto = FieldActivityInsertDTO.builder()
                .cropUuid(crop.getUuid())
                .activityDate(SPRAY_DATE)
                .type(ActivityType.OBSERVATION)
                .pestId(pest.getId())
                .severity(SeverityLevel.MEDIUM)
                .build();

        assertNotNull(fieldActivityService.saveActivity(dto, OWNER).uuid());
    }

    @Test
    @DisplayName("Η συγκομιδή πριν λήξει ο χρόνος αναμονής απορρίπτεται")
    void harvestBeforeTheIntervalExpiresIsRejected() throws Exception {
        spray(SPRAY_DATE);

        LocalDate tooEarly = SPRAY_DATE.plusDays(PHI_DAYS - 1);

        AppObjectInvalidArgumentException e = assertThrows(AppObjectInvalidArgumentException.class,
                () -> fieldActivityService.saveActivity(harvest(tooEarly), OWNER));

        assertTrue(e.getMessage().contains("pre-harvest interval"));
    }

    @Test
    @DisplayName("Η συγκομιδή μετά τη λήξη του χρόνου αναμονής γίνεται δεκτή")
    void harvestAfterTheIntervalExpiresIsAccepted() throws Exception {
        spray(SPRAY_DATE);

        assertNotNull(fieldActivityService
                .saveActivity(harvest(SPRAY_DATE.plusDays(PHI_DAYS)), OWNER).uuid());
    }

    @Test
    @DisplayName("Δεσμευτικός είναι ο ψεκασμός με τη μεγαλύτερη λήξη αναμονής, όχι ο πιο πρόσφατος")
    void theBindingSprayingIsNotNecessarilyTheLatest() throws Exception {
        spray(SPRAY_DATE);

        Product shortInterval = new Product();
        shortInterval.setName("Εντομοκτόνο δοκιμής");
        shortInterval.setActiveSubstance("deltamethrin");
        shortInterval.setCategory(ProductCategory.INSECTICIDE);
        shortInterval.setPreHarvestIntervalDays(5);
        productRepository.save(shortInterval);

        fieldActivityService.saveActivity(FieldActivityInsertDTO.builder()
                .cropUuid(crop.getUuid())
                .activityDate(SPRAY_DATE.plusDays(20))
                .type(ActivityType.SPRAYING)
                .productId(shortInterval.getId())
                .quantity(new BigDecimal("0.5"))
                .unit(UnitOfMeasure.LITRE)
                .build(), OWNER);

        // Ο δεύτερος ψεκασμός λήγει στις +25, ο πρώτος στις +35.
        assertThrows(AppObjectInvalidArgumentException.class,
                () -> fieldActivityService.saveActivity(harvest(SPRAY_DATE.plusDays(30)), OWNER));
    }

    @Test
    @DisplayName("Δεύτερη συγκομιδή στην ίδια καλλιέργεια απορρίπτεται")
    void secondHarvestIsRejected() throws Exception {
        fieldActivityService.saveActivity(harvest(SPRAY_DATE), OWNER);

        assertThrows(AppObjectInvalidArgumentException.class,
                () -> fieldActivityService.saveActivity(harvest(SPRAY_DATE.plusDays(1)), OWNER));
    }

    @Test
    @DisplayName("Εργασία με ημερομηνία μετά τη συγκομιδή απορρίπτεται")
    void activityAfterTheHarvestIsRejected() throws Exception {
        fieldActivityService.saveActivity(harvest(SPRAY_DATE), OWNER);

        assertThrows(AppObjectInvalidArgumentException.class,
                () -> spray(SPRAY_DATE.plusDays(1)));
    }

    @Test
    @DisplayName("Καταχώρηση σε ξένη καλλιέργεια απορρίπτεται")
    void writingToAnotherFarmersCropIsRejected() {
        assertThrows(AppObjectNotAuthorizedException.class,
                () -> fieldActivityService.saveActivity(harvest(SPRAY_DATE), INTRUDER));
    }
}
