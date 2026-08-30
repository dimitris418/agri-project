package gr.aueb.cf.agriapp.model;

import gr.aueb.cf.agriapp.core.enums.ActivityType;
import gr.aueb.cf.agriapp.core.enums.SeverityLevel;
import gr.aueb.cf.agriapp.core.enums.UnitOfMeasure;
import gr.aueb.cf.agriapp.model.static_data.Pest;
import gr.aueb.cf.agriapp.model.static_data.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Μία εγγραφή στο ημερολόγιο αγρού. Ένας πίνακας για όλους τους τύπους
 * εργασίας -- τα πεδία που δεν αφορούν τον εκάστοτε τύπο μένουν null
 * και η συμπλήρωσή τους επιβάλλεται στο service layer.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "field_activities")
public class FieldActivity extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, updatable = false)
    private String uuid;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    /** Σκεύασμα από τον παραμετρικό κατάλογο -- SPRAYING, FERTILIZATION. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    /** Δόση, όγκος ή βάρος, ανάλογα με τον τύπο. */
    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    private UnitOfMeasure unit;

    /** Εχθρός ή ασθένεια από τον παραμετρικό κατάλογο -- OBSERVATION. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pest_id")
    private Pest pest;

    @Enumerated(EnumType.STRING)
    private SeverityLevel severity;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @PrePersist
    public void initializeUUID() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
    }
}
