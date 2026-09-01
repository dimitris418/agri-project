package gr.aueb.cf.agriapp.model;

import gr.aueb.cf.agriapp.model.static_data.CropType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Καλλιέργεια μιας συγκεκριμένης περιόδου πάνω σε ένα αγροτεμάχιο.
 * Το ίδιο αγροτεμάχιο μπορεί να έχει διαφορετική καλλιέργεια ανά έτος.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "crops")
public class Crop extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, updatable = false)
    private String uuid;

    /** Είδος σιτηρού, από τον παραμετρικό κατάλογο. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "crop_type_id", nullable = false)
    private CropType cropType;

    private String variety;

    @Column(nullable = false)
    private Integer cultivationYear;

    private LocalDate plantingDate;

    /**
     * Προγραμματισμένη ημερομηνία συγκομιδής. Είναι πρόβλεψη του παραγωγού,
     * ΟΧΙ η πραγματική συγκομιδή: αυτή προκύπτει από την εργασία τύπου
     * HARVEST στο ημερολόγιο και δεν αποθηκεύεται δεύτερη φορά εδώ.
     */
    private LocalDate expectedHarvestDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcel_id", nullable = false)
    private Parcel parcel;

    @Getter(AccessLevel.PROTECTED)
    @OneToMany(mappedBy = "crop", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<FieldActivity> activities = new HashSet<>();

    public Set<FieldActivity> getAllActivities() {
        if (activities == null) activities = new HashSet<>();
        return Collections.unmodifiableSet(activities);
    }

    public void addActivity(FieldActivity activity) {
        if (activities == null) activities = new HashSet<>();
        activities.add(activity);
        activity.setCrop(this);
    }

    public void removeActivity(FieldActivity activity) {
        if (activities == null) return;
        activities.remove(activity);
        activity.setCrop(null);
    }

    @PrePersist
    public void initializeUUID() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
    }
}
