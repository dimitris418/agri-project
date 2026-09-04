package gr.aueb.cf.agriapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.agriapp.dto.AuthenticationRequestDTO;
import gr.aueb.cf.agriapp.dto.FarmerInsertDTO;
import gr.aueb.cf.agriapp.dto.UserInsertDTO;
import gr.aueb.cf.agriapp.model.auth.Role;
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
class AuthRestControllerTest {

    private static final String USERNAME = "owner@example.com";
    private static final String PASSWORD = "Agri2026!";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;

    @BeforeEach
    void setup() {
        Role role = new Role();
        role.setName("FARMER");
        roleRepository.save(role);
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private FarmerInsertDTO validRegistration() {
        return FarmerInsertDTO.builder()
                .registryNumber("123456")
                .phone("6912345678")
                .userInsertDTO(UserInsertDTO.builder()
                        .firstname("Δημήτρης")
                        .lastname("Παπαδάκης")
                        .username(USERNAME)
                        .password(PASSWORD)
                        .vat("123456789")
                        .build())
                .build();
    }

    private void register() throws Exception {
        mockMvc.perform(post("/api/farmers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRegistration())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Η εγγραφή είναι δημόσια και επιστρέφει 201 με Location")
    void registrationIsPublicAndReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/farmers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRegistration())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.userReadOnlyDTO.role").value("FARMER"))
                .andExpect(jsonPath("$.userReadOnlyDTO.password").doesNotExist());
    }

    @Test
    @DisplayName("Άκυρη εγγραφή επιστρέφει 400 με χάρτη πεδίων")
    void invalidRegistrationReturnsFieldErrors() throws Exception {
        FarmerInsertDTO invalid = FarmerInsertDTO.builder()
                .userInsertDTO(UserInsertDTO.builder()
                        .firstname("Δ")
                        .lastname("")
                        .username("not-an-email")
                        .password("weak")
                        .vat("123")
                        .build())
                .build();

        mockMvc.perform(post("/api/farmers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['userInsertDTO.username']").exists())
                .andExpect(jsonPath("$['userInsertDTO.vat']").exists());
    }

    @Test
    @DisplayName("Η σύνδεση επιστρέφει token")
    void authenticationReturnsAToken() throws Exception {
        register();

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AuthenticationRequestDTO(USERNAME, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.firstname").value("Δημήτρης"));
    }

    @Test
    @DisplayName("Λάθος συνθηματικό επιστρέφει 401 χωρίς να αποκαλύπτει τι έφταιξε")
    void wrongPasswordReturnsUnauthorized() throws Exception {
        register();

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AuthenticationRequestDTO(USERNAME, "WrongPass1!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.description").value("Invalid username or password"));
    }

    @Test
    @DisplayName("Άγνωστος χρήστης επιστρέφει το ίδιο 401")
    void unknownUserReturnsTheSameUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AuthenticationRequestDTO("nobody@example.com", PASSWORD))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.description").value("Invalid username or password"));
    }

    @Test
    @DisplayName("Προστατευμένο endpoint χωρίς token επιστρέφει 401")
    void aProtectedEndpointWithoutATokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/parcels"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UserNotAuthenticated"));
    }

    @Test
    @DisplayName("Άκυρο token επιστρέφει 401")
    void anInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/parcels").header("Authorization", "Bearer not.a.token"))
                .andExpect(status().isUnauthorized());
    }
}
