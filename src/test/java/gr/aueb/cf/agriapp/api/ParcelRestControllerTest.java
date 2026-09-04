package gr.aueb.cf.agriapp.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.agriapp.dto.*;
import gr.aueb.cf.agriapp.model.auth.Capability;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.model.Farmer;
import gr.aueb.cf.agriapp.model.User;
import gr.aueb.cf.agriapp.repository.FarmerRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ParcelRestControllerTest {

    private static final String PASSWORD = "Agri2026!";
    private static final String OWNER = "owner@example.com";
    private static final String OTHER = "other@example.com";
    private static final String READER = "reader@example.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;
    @Autowired private FarmerRepository farmerRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PersistenceContext private EntityManager em;

    @BeforeEach
    void setup() {
        Capability manage = capability("MANAGE_PARCELS");
        Capability view = capability("VIEW_REPORTS");

        Role farmer = new Role();
        farmer.setName("FARMER");
        farmer.getCapabilities().add(manage);
        farmer.getCapabilities().add(view);
        roleRepository.save(farmer);

        Role readOnly = new Role();
        readOnly.setName("READER");
        readOnly.getCapabilities().add(view);
        roleRepository.save(readOnly);
    }

    private Capability capability(String name) {
        Capability c = new Capability();
        c.setName(name);
        em.persist(c);
        return c;
    }

    private void createUserWithRole(String username, String vat, String roleName) {
        Role role = roleRepository.findByName(roleName).orElseThrow();

        User user = new User();
        user.setFirstname("Read");
        user.setLastname("Only");
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setVat(vat);
        user.setIsActive(true);
        user.setRole(role);

        Farmer farmer = new Farmer();
        farmer.setUuid(UUID.randomUUID().toString());
        farmer.setIsActive(true);
        farmer.setUser(user);
        farmerRepository.save(farmer);
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private void register(String username, String vat) throws Exception {
        FarmerInsertDTO dto = FarmerInsertDTO.builder()
                .registryNumber(vat)
                .phone("6912345678")
                .userInsertDTO(UserInsertDTO.builder()
                        .firstname("Test").lastname("Farmer")
                        .username(username).password(PASSWORD).vat(vat)
                        .build())
                .build();

        mockMvc.perform(post("/api/farmers").contentType(MediaType.APPLICATION_JSON).content(json(dto)))
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

    private ParcelInsertDTO parcel(String name, String kaek) {
        return ParcelInsertDTO.builder()
                .name(name).location("Λάρισα")
                .areaInStremmas(new BigDecimal("25.50"))
                .kaek(kaek).isActive(true)
                .build();
    }

    private JsonNode createParcel(String token, String name, String kaek) throws Exception {
        String body = mockMvc.perform(post("/api/parcels")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(parcel(name, kaek))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body);
    }

    @Test
    @DisplayName("Δημιουργία και ανάκτηση αγροτεμαχίου με έγκυρο token")
    void createAndFetchWithAValidToken() throws Exception {
        register(OWNER, "111111111");
        String token = tokenFor(OWNER);

        JsonNode created = createParcel(token, "Κάτω χωράφι", "123456789012");

        mockMvc.perform(get("/api/parcels/" + created.get("uuid").asText())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Κάτω χωράφι"));
    }

    @Test
    @DisplayName("Ανάκτηση ξένου αγροτεμαχίου επιστρέφει 403")
    void fetchingAnotherFarmersParcelIsForbidden() throws Exception {
        register(OWNER, "111111111");
        register(OTHER, "222222222");

        JsonNode created = createParcel(tokenFor(OWNER), "Κάτω χωράφι", "123456789012");

        mockMvc.perform(get("/api/parcels/" + created.get("uuid").asText())
                        .header("Authorization", tokenFor(OTHER)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Η λίστα δείχνει μόνο τα δικά μας αγροτεμάχια")
    void theListShowsOnlyOurOwnParcels() throws Exception {
        register(OWNER, "111111111");
        register(OTHER, "222222222");

        createParcel(tokenFor(OWNER), "Δικό μου", "123456789012");
        createParcel(tokenFor(OTHER), "Ξένο", "999999999999");

        mockMvc.perform(get("/api/parcels").header("Authorization", tokenFor(OWNER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Δικό μου"));
    }

    @Test
    @DisplayName("Άκυρο σώμα επιστρέφει 400 με χάρτη πεδίων")
    void anInvalidBodyReturnsFieldErrors() throws Exception {
        register(OWNER, "111111111");

        ParcelInsertDTO invalid = ParcelInsertDTO.builder()
                .name("")
                .areaInStremmas(new BigDecimal("-5"))
                .kaek("abc")
                .isActive(true)
                .build();

        mockMvc.perform(post("/api/parcels")
                        .header("Authorization", tokenFor(OWNER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.kaek").exists());
    }

    @Test
    @DisplayName("Ασυμφωνία uuid σε path και σώμα επιστρέφει 400")
    void aMismatchBetweenPathAndBodyReturnsBadRequest() throws Exception {
        register(OWNER, "111111111");
        String token = tokenFor(OWNER);

        JsonNode created = createParcel(token, "Κάτω χωράφι", "123456789012");

        ParcelUpdateDTO dto = ParcelUpdateDTO.builder()
                .id(created.get("id").asLong())
                .uuid("some-other-uuid")
                .name("Ανανεωμένο").location("Καρδίτσα")
                .areaInStremmas(new BigDecimal("30.00"))
                .kaek("123456789012").isActive(true)
                .build();

        mockMvc.perform(put("/api/parcels/" + created.get("uuid").asText())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Η διαγραφή επιστρέφει 204 και το αγροτεμάχιο γίνεται ανενεργό")
    void deletionReturnsNoContentAndDeactivates() throws Exception {
        register(OWNER, "111111111");
        String token = tokenFor(OWNER);

        JsonNode created = createParcel(token, "Κάτω χωράφι", "123456789012");
        String uuid = created.get("uuid").asText();

        mockMvc.perform(delete("/api/parcels/" + uuid).header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/parcels/" + uuid).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @DisplayName("Χρήστης χωρίς το capability MANAGE_PARCELS δεν μπορεί να δημιουργήσει")
    void aUserWithoutTheManageCapabilityCannotCreate() throws Exception {
        createUserWithRole(READER, "333333333", "READER");
        String token = tokenFor(READER);

        mockMvc.perform(get("/api/parcels").header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/parcels")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(parcel("Απαγορευμένο", "123456789012"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UserNotAuthorized"));
    }
}
