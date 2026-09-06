package gr.aueb.cf.agriapp.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.agriapp.core.enums.ActivityType;
import gr.aueb.cf.agriapp.core.enums.CropSeason;
import gr.aueb.cf.agriapp.core.enums.UnitOfMeasure;
import gr.aueb.cf.agriapp.dto.*;
import gr.aueb.cf.agriapp.model.auth.Capability;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.model.static_data.CropType;
import gr.aueb.cf.agriapp.repository.CropTypeRepository;
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
class CropRestControllerTest {

    private static final String PASSWORD = "Agri2026!";
    private static final String OWNER = "owner@example.com";
    private static final String OTHER = "other@example.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CropTypeRepository cropTypeRepository;

    @PersistenceContext private EntityManager em;

    private String token;
    private String otherToken;
    private String parcelUuid;
    private String otherParcelUuid;
    private Long wheatId;
    private Long maizeId;

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

        wheatId = cropType("Σκληρό σιτάρι", "Triticum durum", CropSeason.WINTER);
        maizeId = cropType("Αραβόσιτος", "Zea mays", CropSeason.SPRING);

        register(OWNER, "111111", "111111111");
        register(OTHER, "222222", "222222222");
        token = tokenFor(OWNER);
        otherToken = tokenFor(OTHER);

        parcelUuid = createParcel(token, "Κάτω χωράφι");
        otherParcelUuid = createParcel(otherToken, "Ξένο χωράφι");
    }

    private Long cropType(String name, String latinName, CropSeason season) {
        CropType type = new CropType();
        type.setName(name);
        type.setLatinName(latinName);
        type.setSeason(season);
        return cropTypeRepository.save(type).getId();
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private void register(String username, String registryNumber, String vat) throws Exception {
        FarmerInsertDTO dto = FarmerInsertDTO.builder()
                .registryNumber(registryNumber).phone("6912345678")
                .userInsertDTO(UserInsertDTO.builder()
                        .firstname("Test").lastname("Farmer")
                        .username(username).password(PASSWORD).vat(vat)
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

    private JsonNode create(String bearer, String path, Object body) throws Exception {
        String response = mockMvc.perform(post(path)
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response);
    }

    private String createParcel(String bearer, String name) throws Exception {
        return create(bearer, "/api/parcels", ParcelInsertDTO.builder()
                .name(name).location("Λάρισα")
                .areaInStremmas(new BigDecimal("25.50")).kaek("").isActive(true)
                .build()).get("uuid").asText();
    }

    private CropInsertDTO crop(String parcel, Long typeId, int year) {
        return CropInsertDTO.builder()
                .parcelUuid(parcel).cropTypeId(typeId)
                .variety("Σίμετο").cultivationYear(year)
                .plantingDate(LocalDate.of(year - 1, 11, 10))
                .expectedHarvestDate(LocalDate.of(year, 6, 20))
                .build();
    }

    @Test
    @DisplayName("Η καλλιέργεια δημιουργείται με το είδος της ένθετο")
    void aCropIsCreatedWithItsTypeNested() throws Exception {
        mockMvc.perform(post("/api/crops")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(crop(parcelUuid, wheatId, 2026))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.cropTypeReadOnlyDTO.name").value("Σκληρό σιτάρι"))
                .andExpect(jsonPath("$.cropTypeReadOnlyDTO.season").value("WINTER"))
                .andExpect(jsonPath("$.parcelReadOnlyDTO.name").value("Κάτω χωράφι"))
                .andExpect(jsonPath("$.harvestDate").doesNotExist());
    }

    @Test
    @DisplayName("Καλλιέργεια σε ξένο αγροτεμάχιο απορρίπτεται με 403")
    void aCropOnAForeignParcelIsRejected() throws Exception {
        mockMvc.perform(post("/api/crops")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(crop(otherParcelUuid, wheatId, 2026))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ParcelNotAuthorized"));
    }

    @Test
    @DisplayName("Ξένη καλλιέργεια δεν είναι ορατή στον άλλο αγρότη")
    void aForeignCropIsNotVisible() throws Exception {
        String uuid = create(otherToken, "/api/crops", crop(otherParcelUuid, wheatId, 2026))
                .get("uuid").asText();

        mockMvc.perform(get("/api/crops/" + uuid).header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CropNotAuthorized"));
    }

    @Test
    @DisplayName("Η ενημέρωση αλλάζει ποικιλία και είδος καλλιέργειας")
    void updatingChangesTheVarietyAndTheType() throws Exception {
        JsonNode created = create(token, "/api/crops", crop(parcelUuid, wheatId, 2026));

        CropUpdateDTO dto = CropUpdateDTO.builder()
                .id(created.get("id").asLong()).uuid(created.get("uuid").asText())
                .cropTypeId(maizeId).variety("Pioneer").cultivationYear(2026)
                .plantingDate(LocalDate.of(2026, 4, 1))
                .expectedHarvestDate(LocalDate.of(2026, 9, 15))
                .build();

        mockMvc.perform(put("/api/crops/" + dto.uuid())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variety").value("Pioneer"))
                .andExpect(jsonPath("$.cropTypeReadOnlyDTO.name").value("Αραβόσιτος"));
    }

    @Test
    @DisplayName("Ασυμφωνία uuid ανάμεσα σε path και body επιστρέφει 400")
    void aUuidMismatchBetweenPathAndBodyReturnsBadRequest() throws Exception {
        JsonNode created = create(token, "/api/crops", crop(parcelUuid, wheatId, 2026));

        CropUpdateDTO dto = CropUpdateDTO.builder()
                .id(created.get("id").asLong()).uuid(created.get("uuid").asText())
                .cropTypeId(wheatId).variety("Σίμετο").cultivationYear(2026)
                .build();

        mockMvc.perform(put("/api/crops/some-other-uuid")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CropInvalidArgument"));
    }

    @Test
    @DisplayName("Έτος καλλιέργειας εκτός ορίων επιστρέφει 400 με το όνομα του πεδίου")
    void anOutOfRangeCultivationYearReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/crops")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(crop(parcelUuid, wheatId, 1999))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cultivationYear").exists());
    }

    @Test
    @DisplayName("Η αναζήτηση φιλτράρεται ανά έτος και επιστρέφει μόνο τις δικές μου καλλιέργειες")
    void theSearchIsFilteredByYearAndScopedToTheOwner() throws Exception {
        create(token, "/api/crops", crop(parcelUuid, wheatId, 2025));
        create(token, "/api/crops", crop(parcelUuid, maizeId, 2026));
        create(otherToken, "/api/crops", crop(otherParcelUuid, wheatId, 2026));

        mockMvc.perform(get("/api/crops").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/crops").header("Authorization", token)
                        .param("cultivationYear", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.data[0].cropTypeReadOnlyDTO.name").value("Αραβόσιτος"))
                .andExpect(jsonPath("$.data[0].parcelReadOnlyDTO.name").value("Κάτω χωράφι"));

        mockMvc.perform(get("/api/crops").header("Authorization", token)
                        .param("parcelUuid", otherParcelUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Άγνωστη στήλη ταξινόμησης δεν ρίχνει το endpoint")
    void anUnknownSortColumnFallsBackToTheDefault() throws Exception {
        create(token, "/api/crops", crop(parcelUuid, wheatId, 2026));

        mockMvc.perform(get("/api/crops").header("Authorization", token)
                        .param("sortBy", "parcel.farmer.user.password")
                        .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Καλλιέργεια με εγγραφές στο ημερολόγιο δεν διαγράφεται")
    void aCropWithLogbookEntriesCannotBeDeleted() throws Exception {
        String uuid = create(token, "/api/crops", crop(parcelUuid, wheatId, 2026))
                .get("uuid").asText();

        create(token, "/api/activities", FieldActivityInsertDTO.builder()
                .cropUuid(uuid).activityDate(LocalDate.of(2026, 3, 10))
                .type(ActivityType.IRRIGATION)
                .quantity(new BigDecimal("30")).unit(UnitOfMeasure.CUBIC_METER)
                .build());

        mockMvc.perform(delete("/api/crops/" + uuid).header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CropInvalidArgument"));
    }

    @Test
    @DisplayName("Καλλιέργεια χωρίς εγγραφές διαγράφεται με 204")
    void aCropWithoutEntriesIsDeleted() throws Exception {
        String uuid = create(token, "/api/crops", crop(parcelUuid, wheatId, 2026))
                .get("uuid").asText();

        mockMvc.perform(delete("/api/crops/" + uuid).header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/crops/" + uuid).header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CropNotFound"));
    }
}
