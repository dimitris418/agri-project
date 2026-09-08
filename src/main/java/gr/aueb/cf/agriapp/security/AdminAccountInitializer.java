package gr.aueb.cf.agriapp.security;

import gr.aueb.cf.agriapp.model.User;
import gr.aueb.cf.agriapp.model.auth.Role;
import gr.aueb.cf.agriapp.repository.RoleRepository;
import gr.aueb.cf.agriapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Δημιουργεί τον λογαριασμό διαχειριστή στην εκκίνηση, εφόσον δοθούν οι
 * μεταβλητές περιβάλλοντος και ο χρήστης δεν υπάρχει ήδη. Η εγγραφή μέσω
 * του API δίνει πάντα ρόλο FARMER, οπότε ο πρώτος ADMIN δεν θα μπορούσε
 * να δημιουργηθεί αλλιώς χωρίς να μπει συνθηματικό στο repository.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminAccountInitializer implements ApplicationRunner {

    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}")
    private String username;

    @Value("${app.admin.password:}")
    private String password;

    @Value("${app.admin.vat:}")
    private String vat;

    @Value("${app.admin.firstname:Διαχειριστής}")
    private String firstname;

    @Value("${app.admin.lastname:Συστήματος}")
    private String lastname;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank() || vat.isBlank()) {
            log.info("Admin account is not configured; skipping creation");
            return;
        }

        if (userRepository.findByUsername(username).isPresent()) return;

        Role adminRole = roleRepository.findByName(ADMIN_ROLE).orElse(null);
        if (adminRole == null) {
            log.warn("Role {} is missing from the database; the admin account was not created", ADMIN_ROLE);
            return;
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setFirstname(firstname);
        admin.setLastname(lastname);
        admin.setVat(vat);
        admin.setIsActive(true);
        admin.setRole(adminRole);

        userRepository.save(admin);
        log.info("Admin account username={} created", username);
    }
}
