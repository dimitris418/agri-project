package gr.aueb.cf.agriapp.model.static_data;

import gr.aueb.cf.agriapp.core.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Παραμετρικός πίνακας σκευασμάτων -- φυτοπροστατευτικά και λιπάσματα.
 * Ο χρόνος αναμονής πριν τη συγκομιδή είναι ιδιότητα ΤΟΥ ΣΚΕΥΑΣΜΑΤΟΣ,
 * όχι της εργασίας: έτσι ο σχετικός έλεγχος δεν εξαρτάται από το τι
 * θα πληκτρολογήσει ο χρήστης.
 *
 * ΣΗΜΕΙΩΣΗ ΓΙΑ ΤΟ ΕΥΡΟΣ ΤΟΥ ΚΑΤΑΛΟΓΟΥ: οι εγγραφές είναι σε επίπεδο
 * δραστικής ουσίας και έχουν επιδεικτικό χαρακτήρα. Μια παραγωγική
 * εφαρμογή θα τηρούσε εμπορικά σκευάσματα, τροφοδοτούμενα από το
 * επίσημο μητρώο φυτοπροστατευτικών προϊόντων του ΥΠΑΑΤ.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String activeSubstance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    /**
     * Χρόνος αναμονής σε ημέρες πριν τη συγκομιδή (PHI). Null για τα λιπάσματα,
     * που δεν έχουν χρόνο αναμονής -- ο σχετικός έλεγχος τα προσπερνά.
     *
     * ΠΡΟΣΟΧΗ: οι τιμές που φορτώνονται από το products.sql είναι ΕΝΔΕΙΚΤΙΚΕΣ.
     * Ο πραγματικός χρόνος αναμονής ορίζεται ανά εμπορικό σκεύασμα ΚΑΙ ανά
     * καλλιέργεια στην έγκριση κυκλοφορίας του, επομένως δεν μπορεί να
     * αποδοθεί σωστά σε επίπεδο δραστικής ουσίας. Δεν πρέπει να
     * χρησιμοποιηθούν ως γεωπονική οδηγία.
     */
    private Integer preHarvestIntervalDays;
}
