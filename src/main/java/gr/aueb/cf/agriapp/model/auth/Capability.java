package gr.aueb.cf.agriapp.model.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Διακριτό δικαίωμα ενέργειας, π.χ. MANAGE_PARCELS. Ένας ρόλος
 * συγκεντρώνει πολλά capabilities και το endpoint ελέγχει το capability,
 * όχι τον ρόλο -- έτσι αλλάζουν τα δικαιώματα χωρίς αλλαγή κώδικα.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "capabilities")
public class Capability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @ManyToMany(mappedBy = "capabilities", fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    // Helper Methods
    public void addRole(Role role) {
        this.roles.add(role);
        role.getCapabilities().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getCapabilities().remove(this);
    }
}
