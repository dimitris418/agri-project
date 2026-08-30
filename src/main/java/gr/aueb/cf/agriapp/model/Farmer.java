package gr.aueb.cf.agriapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Ο αγρότης -- ο μοναδικός actor της εφαρμογής.
 * Είναι η "domain" όψη ενός User: κρατάει τα επαγγελματικά του στοιχεία
 * και είναι ο ιδιοκτήτης των αγροτεμαχίων.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "farmers")
public class Farmer extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Δημόσιο αναγνωριστικό -- εκτίθεται στο API αντί του id. */
    @Column(unique = true, updatable = false)
    private String uuid;

    /** Αριθμός Μητρώου Αγροτών (ΜΑΑΕ). */
    @Column(unique = true)
    private String registryNumber;

    private String phone;

    @ColumnDefault("true")
    private Boolean isActive;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Getter(AccessLevel.PROTECTED)
    @OneToMany(mappedBy = "farmer", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Parcel> parcels = new HashSet<>();

    public Set<Parcel> getAllParcels() {
        if (parcels == null) parcels = new HashSet<>();
        return Collections.unmodifiableSet(parcels);
    }

    public void addParcel(Parcel parcel) {
        if (parcels == null) parcels = new HashSet<>();
        parcels.add(parcel);
        parcel.setFarmer(this);
    }

    public void removeParcel(Parcel parcel) {
        if (parcels == null) return;
        parcels.remove(parcel);
        parcel.setFarmer(null);
    }

    @PrePersist
    public void initializeUUID() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
    }
}
