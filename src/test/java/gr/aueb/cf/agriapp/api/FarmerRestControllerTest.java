package gr.aueb.cf.agriapp.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.agriapp.dto.*;
import gr.aueb.cf.agriapp.model.User;
import gr.aueb.cf.agriapp.model.auth.Capability;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FarmerRestControllerTest {

    private static final String PASSWORD = "Agri2026!";
    private static final String USERNAME = "owner@example.com";
    private static final String OTHER = "other@example.com";
    private static final String ADMIN = "admin@example.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PersistenceContext private EntityManager em;

    private String token;
    private JsonNode me;

    @BeforeEach
    void setup() throws Exception {
        Role role = new Role();
        role.setName("FARMER");
        roleRepository.save(role);

        FarmerInsertDTO dto = FarmerInsertDTO.builder()
                .registryNumber("123456").phone("6912345678")
                .userInsertDTO(UserInsertDTO.builder()
                        .firstname("Δημήτρης").lastname("Παπαδάκης")
                        .username(USERNAME).password(PASSWORD).vat("123456789")
                        .build())
                .build();

        String body = mockMvc.perform(post("/api/farmers")
                        .contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        me = objectMapper.readTree(body);
        token = tokenFor(USERNAME);
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private String tokenFor(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AuthenticationRequestDTO(username, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return "Bearer " + objectMapper.readTree(body).get("token").asText();
    }

    private String registerFarmer(String username, String vat, String lastname) throws Exception {
        FarmerInsertDTO dto = FarmerInsertDTO.builder()
                .registryNumber(vat).phone("6912345678")
                .userInsertDTO(UserInsertDTO.builder()
                        .firstname("Ελένη").lastname(lastname)
                        .username(username).password(PASSWORD).vat(vat)
                        .build())
                .build();

        String body = mockMvc.perform(post("/api/farmers")
                        .contentType(MediaType.APPLICATION_JSON).content(json(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("uuid").asText();
    }

    private String statusJson(boolean isActive) throws Exception {
        return json(FarmerStatusUpdateDTO.builder().isActive(isActive).build());
    }

    private String adminToken() throws Exception {
        Capability manageUsers = new Capability();
        manageUsers.setName("MANAGE_USERS");
        em.persist(manageUsers);

        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.getCapabilities().add(manageUsers);
        roleRepository.save(adminRole);

        User admin = new User();
        admin.setFirstname("Διαχειριστής");
        admin.setLastname("Συστήματος");
        admin.setUsername(ADMIN);
        admin.setPassword(passwordEncoder.encode(PASSWORD));
        admin.setVat("999999999");
        admin.setIsActive(true);
        admin.setRole(adminRole);
        userRepository.save(admin);

        return tokenFor(ADMIN);
    }

    private FarmerUpdateDTO updateDTO(String password, String phone) {
        return FarmerUpdateDTO.builder()
                .id(me.get("id").asLong())
                .uuid(me.get("uuid").asText())
                .registryNumber("654321")
                .phone(phone)
                .isActive(true)
                .userUpdateDTO(UserUpdateDTO.builder()
                        .id(userRepository.findByUsername(USERNAME).orElseThrow().getId())
                        .firstname("Δημήτριος").lastname("Παπαδάκης")
                        .username(USERNAME).password(password).vat("123456789")
                        .build())
                .build();
    }

    @Test
    @DisplayName("Το προφίλ επιστρέφεται χωρίς συνθηματικό")
    void theProfileIsReturnedWithoutThePassword() throws Exception {
        mockMvc.perform(get("/api/farmers/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userReadOnlyDTO.username").value(USERNAME))
                .andExpect(jsonPath("$.userReadOnlyDTO.role").value("FARMER"))
                .andExpect(jsonPath("$.userReadOnlyDTO.password").doesNotExist());
    }

    @Test
    @DisplayName("Το προφίλ απαιτεί token")
    void theProfileRequiresAToken() throws Exception {
        mockMvc.perform(get("/api/farmers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Η ενημέρωση χωρίς συνθηματικό δεν εμποδίζει επόμενη σύνδεση")
    void updatingWithoutAPasswordDoesNotBreakLogin() throws Exception {
        mockMvc.perform(put("/api/farmers/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateDTO(null, "6900000000"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("6900000000"));

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AuthenticationRequestDTO(USERNAME, PASSWORD))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Ο διαχειριστής βλέπει τους εγγεγραμμένους αγρότες με σελιδοποίηση")
    void theAdminListsTheRegisteredFarmers() throws Exception {
        registerFarmer(OTHER, "987654321", "Μανωλάκη");
        String adminToken = adminToken();

        mockMvc.perform(get("/api/farmers")
                        .header("Authorization", adminToken)
                        .param("sortBy", "user.lastname"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.data[0].userReadOnlyDTO.lastname").value("Μανωλάκη"))
                .andExpect(jsonPath("$.data[0].userReadOnlyDTO.password").doesNotExist());

        mockMvc.perform(get("/api/farmers")
                        .header("Authorization", adminToken)
                        .param("lastname", "μανωλ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.data[0].userReadOnlyDTO.username").value(OTHER));
    }

    @Test
    @DisplayName("Ο αγρότης δεν βλέπει τη λίστα των υπόλοιπων αγροτών")
    void aFarmerCannotListTheOtherFarmers() throws Exception {
        mockMvc.perform(get("/api/farmers").header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UserNotAuthorized"));
    }

    @Test
    @DisplayName("Ο διαχειριστής απενεργοποιεί και επαναφέρει λογαριασμό αγρότη")
    void theAdminDeactivatesAndRestoresAFarmer() throws Exception {
        String uuid = registerFarmer(OTHER, "987654321", "Μανωλάκη");
        String adminToken = adminToken();

        mockMvc.perform(patch("/api/farmers/" + uuid + "/status")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(statusJson(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        mockMvc.perform(patch("/api/farmers/" + uuid + "/status")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(statusJson(true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("Το token απενεργοποιημένου αγρότη παύει να γίνεται δεκτό")
    void theTokenOfADeactivatedFarmerStopsWorking() throws Exception {
        String uuid = registerFarmer(OTHER, "987654321", "Μανωλάκη");
        String farmerToken = tokenFor(OTHER);

        mockMvc.perform(get("/api/farmers/me").header("Authorization", farmerToken))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/farmers/" + uuid + "/status")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content(statusJson(false)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/farmers/me").header("Authorization", farmerToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UserNotAuthenticated"));
    }

    @Test
    @DisplayName("Ο απενεργοποιημένος αγρότης δεν μπορεί να ξανασυνδεθεί")
    void aDeactivatedFarmerCannotAuthenticateAgain() throws Exception {
        String uuid = registerFarmer(OTHER, "987654321", "Μανωλάκη");

        mockMvc.perform(patch("/api/farmers/" + uuid + "/status")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content(statusJson(false)))
                .andExpect(status().isOk());

        // Τα στοιχεία είναι σωστά· ο έλεγχος isEnabled() του Spring Security
        // κόβει τη σύνδεση πριν καν συγκριθεί το συνθηματικό.
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AuthenticationRequestDTO(OTHER, PASSWORD))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UserNotAuthenticated"));
    }

    @Test
    @DisplayName("Ο αγρότης δεν αλλάζει την κατάσταση κανενός λογαριασμού")
    void aFarmerCannotChangeAnyAccountStatus() throws Exception {
        mockMvc.perform(patch("/api/farmers/" + me.get("uuid").asText() + "/status")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(statusJson(false)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UserNotAuthorized"));
    }

    @Test
    @DisplayName("Άκυρο τηλέφωνο επιστρέφει 400")
    void anInvalidPhoneReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/farmers/me")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateDTO(null, "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phone").exists());
    }
}
