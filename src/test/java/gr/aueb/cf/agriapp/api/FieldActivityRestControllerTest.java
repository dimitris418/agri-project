package gr.aueb.cf.agriapp.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.agriapp.core.enums.*;
import gr.aueb.cf.agriapp.dto.*;
import gr.aueb.cf.agriapp.model.auth.Capability;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.model.static_data.CropType;
import gr.aueb.cf.agriapp.model.static_data.Product;
import gr.aueb.cf.agriapp.repository.CropTypeRepository;
import gr.aueb.cf.agriapp.repository.ProductRepository;
import gr.aueb.cf.agriapp.repository.RoleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FieldActivityRestControllerTest {

    private static final String PASSWORD = "Agri2026!";
    private static final String OWNER = "owner@example.com";
    private static final LocalDate SPRAY_DATE = LocalDate.of(2026, 5, 1);
    private static final int PHI_DAYS = 35;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CropTypeRepository cropTypeRepository;
    @Autowired private ProductRepository productRepository;

    @PersistenceContext private EntityManager em;

    private String token;
    private String cropUuid;
    private Long fungicideId;

    @BeforeEach
    void setup() throws Exception {
        Role farmer = new Role();
        farmer.setName("FARMER");
        for (String name : new String[]{"MANAGE_PARCELS", "RECORD_ACTIVITIES", "VIEW_REPORTS"}) {
            Capability c = new Capability();
            c.setName(name);
            em.persist(c);
            farmer.getCapabilities().add(c);
        }
        roleRepository.save(farmer);

        CropType cropType = new CropType();
        cropType.setName("Σκληρό σιτάρι");
        cropType.setLatinName("Triticum durum");
        cropType.setSeason(CropSeason.WINTER);
        cropTypeRepository.save(cropType);

        Product fungicide = new Product();
        fungicide.setName("Μυκητοκτόνο δοκιμής");
        fungicide.setActiveSubstance("tebuconazole");
        fungicide.setCategory(ProductCategory.FUNGICIDE);
        fungicide.setPreHarvestIntervalDays(PHI_DAYS);
        productRepository.save(fungicide);
        fungicideId = fungicide.getId();

        register();
        token = tokenFor(OWNER);

        String parcelUuid = create("/api/parcels", ParcelInsertDTO.builder()
                .name("Κάτω χωράφι").location("Λάρισα")
                .areaInStremmas(new BigDecimal("25.50")).kaek("").isActive(true)
                .build()).get("uuid").asText();

        cropUuid = create("/api/crops", CropInsertDTO.builder()
                .parcelUuid(parcelUuid).cropTypeId(cropType.getId())
                .variety("Σίμετο").cultivationYear(2026)
                .plantingDate(LocalDate.of(2025, 11, 10))
                .expectedHarvestDate(LocalDate.of(2026, 6, 20))
                .build()).get("uuid").asText();
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private void register() throws Exception {
        FarmerInsertDTO dto = FarmerInsertDTO.builder()
                .registryNumber("123456").phone("6912345678")
                .userInsertDTO(UserInsertDTO.builder()
                        .firstname("Test").lastname("Farmer")
                        .username(OWNER).password(PASSWORD).vat("123456789")
                        .build())
                .build();

        mockMvc.perform(post("/api/farmers")
                        .contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isCreated());
    }

    private String tokenFor(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AuthenticationRequestDTO(username, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return "Bearer " + objectMapper.readTree(body).get("token").asText();
    }

    private JsonNode create(String path, Object body) throws Exception {
        String response = mockMvc.perform(post(path)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response);
    }

    private FieldActivityInsertDTO spraying(LocalDate date) {
        return FieldActivityInsertDTO.builder()
                .cropUuid(cropUuid).activityDate(date).type(ActivityType.SPRAYING)
                .productId(fungicideId).quantity(new BigDecimal("1.5")).unit(UnitOfMeasure.LITRE)
                .build();
    }

    private FieldActivityInsertDTO harvest(LocalDate date) {
        return FieldActivityInsertDTO.builder()
                .cropUuid(cropUuid).activityDate(date).type(ActivityType.HARVEST)
                .quantity(new BigDecimal("8000")).unit(UnitOfMeasure.KILOGRAM)
                .build();
    }

    @Test
    @DisplayName("Ο ψεκασμός καταχωρείται και επιστρέφει το σκεύασμα ένθετο")
    void aSprayingIsRecordedWithItsProduct() throws Exception {
        mockMvc.perform(post("/api/activities")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(spraying(SPRAY_DATE))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.type").value("SPRAYING"))
                .andExpect(jsonPath("$.productReadOnlyDTO.preHarvestIntervalDays").value(PHI_DAYS));
    }

    @Test
    @DisplayName("Ο ψεκασμός χωρίς σκεύασμα επιστρέφει 400 μέσω του API")
    void aSprayingWithoutAProductIsRejectedThroughTheApi() throws Exception {
        FieldActivityInsertDTO dto = FieldActivityInsertDTO.builder()
                .cropUuid(cropUuid).activityDate(SPRAY_DATE).type(ActivityType.SPRAYING)
                .quantity(new BigDecimal("1.5")).unit(UnitOfMeasure.LITRE)
                .build();

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ActivityInvalidArgument"));
    }

    @Test
    @DisplayName("Η συγκομιδή πριν λήξει ο χρόνος αναμονής επιστρέφει 400 μέσω του API")
    void harvestBeforeTheIntervalIsRejectedThroughTheApi() throws Exception {
        create("/api/activities", spraying(SPRAY_DATE));

        mockMvc.perform(post("/api/activities")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(harvest(SPRAY_DATE.plusDays(PHI_DAYS - 1)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ActivityInvalidArgument"))
                .andExpect(jsonPath("$.description").value(
                        org.hamcrest.Matchers.containsString("pre-harvest interval")));
    }

    @Test
    @DisplayName("Η συγκομιδή μετά τη λήξη γίνεται δεκτή και εμφανίζεται στην καλλιέργεια")
    void harvestAfterTheIntervalIsAcceptedAndSurfacesOnTheCrop() throws Exception {
        create("/api/activities", spraying(SPRAY_DATE));
        LocalDate harvestDate = SPRAY_DATE.plusDays(PHI_DAYS);
        create("/api/activities", harvest(harvestDate));

        mockMvc.perform(get("/api/crops/" + cropUuid).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.harvestDate").value(harvestDate.toString()));
    }

    @Test
    @DisplayName("Το ημερολόγιο φιλτράρεται ανά τύπο και ανά εύρος ημερομηνιών")
    void theLogbookIsFilteredByTypeAndDateRange() throws Exception {
        create("/api/activities", spraying(SPRAY_DATE));
        create("/api/activities", harvest(SPRAY_DATE.plusDays(PHI_DAYS)));

        mockMvc.perform(get("/api/activities")
                        .header("Authorization", token)
                        .param("type", "SPRAYING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.data[0].type").value("SPRAYING"));

        mockMvc.perform(get("/api/activities")
                        .header("Authorization", token)
                        .param("dateFrom", SPRAY_DATE.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.data[0].type").value("HARVEST"));
    }

    @Test
    @DisplayName("Η διαγραφή εργασίας επιστρέφει 204")
    void deletingAnActivityReturnsNoContent() throws Exception {
        String uuid = create("/api/activities", spraying(SPRAY_DATE)).get("uuid").asText();

        mockMvc.perform(delete("/api/activities/" + uuid).header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/activities").header("Authorization", token))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
