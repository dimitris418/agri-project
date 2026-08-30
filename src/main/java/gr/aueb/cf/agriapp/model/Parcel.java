package gr.aueb.cf.agriapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Αγροτεμάχιο. Ανήκει πάντα σε έναν αγρότη -- κάθε service method
 * ελέγχει ότι ο συνδεδεμένος χρήστης είναι ο ιδιοκτήτης.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "parcels")
public class Parcel extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, updatable = false)
    private String uuid;

    /** Φιλική ονομασία, π.χ. "Κάτω χωράφι". */
    @Column(nullable = false)
    private String name;

    private String location;

    /** Έκταση σε στρέμματα. */
    @Column(precision = 10, scale = 2)
    private BigDecimal areaInStremmas;

    /** Κωδικός Αριθμός Εθνικού Κτηματολογίου -- προαιρετικός. */
    @Column(unique = true)
    private String kaek;

    @ColumnDefault("true")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @Getter(AccessLevel.PROTECTED)
    @OneToMany(mappedBy = "parcel", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Crop> crops = new HashSet<>();

    public Set<Crop> getAllCrops() {
        if (crops == null) crops = new HashSet<>();
        return Collections.unmodifiableSet(crops);
    }

    public void addCrop(Crop crop) {
        if (crops == null) crops = new HashSet<>();
        crops.add(crop);
        crop.setParcel(this);
    }

    public void removeCrop(Crop crop) {
        if (crops == null) return;
        crops.remove(crop);
        crop.setParcel(null);
    }

    @PrePersist
    public void initializeUUID() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
    }
}
