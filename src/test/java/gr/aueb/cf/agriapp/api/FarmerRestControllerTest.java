package gr.aueb.cf.agriapp.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.agriapp.dto.*;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.repository.RoleRepository;
import gr.aueb.cf.agriapp.repository.UserRepository;
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
class FarmerRestControllerTest {

    private static final String PASSWORD = "Agri2026!";
    private static final String USERNAME = "owner@example.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;

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

        String auth = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new AuthenticationRequestDTO(USERNAME, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        token = "Bearer " + objectMapper.readTree(auth).get("token").asText();
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
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
