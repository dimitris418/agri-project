package gr.aueb.cf.agriapp.model;

import gr.aueb.cf.agriapp.model.auth.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Λογαριασμός χρήστη. Κρατάει ΜΟΝΟ ό,τι αφορά την ταυτοποίηση
 * και την εξουσιοδότηση. Τα δεδομένα του domain ζουν στον Farmer.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "users")
public class User extends AbstractEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(unique = true, nullable = false)
    private String vat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ColumnDefault("true")
    private Boolean isActive;

    @OneToOne(mappedBy = "user")
    private Farmer farmer;

    public boolean isFarmer() {
        return getFarmer() != null;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }

    /**
     * Επιστρέφει τον ρόλο με πρόθεμα ROLE_ (για hasRole) και επιπλέον
     * ένα authority ανά capability (για hasAuthority).
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

        role.getCapabilities().forEach(capability ->
                grantedAuthorities.add(new SimpleGrantedAuthority(capability.getName())));

        return grantedAuthorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.getIsActive() == null || this.getIsActive();
    }
}
