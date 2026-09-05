package gr.aueb.cf.agriapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.agriapp.core.enums.CropSeason;
import gr.aueb.cf.agriapp.core.enums.PestType;
import gr.aueb.cf.agriapp.core.enums.ProductCategory;
import gr.aueb.cf.agriapp.dto.AuthenticationRequestDTO;
import gr.aueb.cf.agriapp.dto.FarmerInsertDTO;
import gr.aueb.cf.agriapp.dto.UserInsertDTO;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.model.static_data.CropType;
import gr.aueb.cf.agriapp.model.static_data.Pest;
import gr.aueb.cf.agriapp.model.static_data.Product;
import gr.aueb.cf.agriapp.repository.CropTypeRepository;
import gr.aueb.cf.agriapp.repository.PestRepository;
import gr.aueb.cf.agriapp.repository.ProductRepository;
import gr.aueb.cf.agriapp.repository.RoleRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class LookupRestControllerTest {

    private static final String PASSWORD = "Agri2026!";
    private static final String USERNAME = "owner@example.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CropTypeRepository cropTypeRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private PestRepository pestRepository;

    private String token;

    @BeforeEach
    void setup() throws Exception {
        Role role = new Role();
        role.setName("FARMER");
        roleRepository.save(role);

        cropTypeRepository.save(cropType("Σκληρό σιτάρι", CropSeason.WINTER));
        cropTypeRepository.save(cropType("Αραβόσιτος", CropSeason.SPRING));

        productRepository.save(product("Μυκητοκτόνο", "tebuconazole", ProductCategory.FUNGICIDE, 35));
        productRepository.save(product("Ουρία", "N 46%", ProductCategory.FERTILIZER, null));

        pestRepository.save(pest("Ωίδιο", PestType.FUNGAL_DISEASE));
        pestRepository.save(pest("Αφίδες", PestType.INSECT));

        register();
        token = tokenFor();
    }

    private CropType cropType(String name, CropSeason season) {
        CropType c = new CropType();
        c.setName(name);
        c.setSeason(season);
        return c;
    }

    private Product product(String name, String substance, ProductCategory category, Integer phi) {
        Product p = new Product();
        p.setName(name);
        p.setActiveSubstance(substance);
        p.setCategory(category);
        p.setPreHarvestIntervalDays(phi);
        return p;
    }

    private Pest pest(String name, PestType type) {
        Pest p = new Pest();
        p.setName(name);
        p.setType(type);
        return p;
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private void register() throws Exception {
        FarmerInsertDTO dto = FarmerInsertDTO.builder()
                .registryNumber("123456").phone("6912345678")
                .userInsertDTO(UserInsertDTO.builder()
                        .firstname("Test").lastname("Farmer")
                        .username(USERNAME).password(PASSWORD).vat("123456789")
                        .build())
                .build();

        mockMvc.perform(post("/api/farmers").contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isCreated());
    }

    private String tokenFor() throws Exception {
        String body = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AuthenticationRequestDTO(USERNAME, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return "Bearer " + objectMapper.readTree(body).get("token").asText();
    }

    @Test
    @DisplayName("Οι κατάλογοι απαιτούν αυθεντικοποίηση")
    void theCataloguesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/lookups/crop-types"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Ο κατάλογος σιτηρών επιστρέφεται ολόκληρος και φιλτραρισμένος ανά περίοδο")
    void cropTypesAreReturnedWholeAndFilteredBySeason() throws Exception {
        mockMvc.perform(get("/api/lookups/crop-types").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/lookups/crop-types").header("Authorization", token)
                        .param("season", "SPRING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Αραβόσιτος"));
    }

    @Test
    @DisplayName("Ο κατάλογος σκευασμάτων φιλτράρεται ανά κατηγορία")
    void productsAreFilteredByCategory() throws Exception {
        mockMvc.perform(get("/api/lookups/products").header("Authorization", token)
                        .param("category", "FERTILIZER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].preHarvestIntervalDays").doesNotExist());
    }

    @Test
    @DisplayName("Ο κατάλογος εχθρών φιλτράρεται ανά τύπο")
    void pestsAreFilteredByType() throws Exception {
        mockMvc.perform(get("/api/lookups/pests").header("Authorization", token)
                        .param("type", "INSECT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Αφίδες"));
    }

    @Test
    @DisplayName("Άκυρη τιμή enum σε παράμετρο επιστρέφει 400")
    void anInvalidEnumParameterReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/lookups/crop-types").header("Authorization", token)
                        .param("season", "AUTUMN"))
                .andExpect(status().isBadRequest());
    }
}
