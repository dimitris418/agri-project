package gr.aueb.cf.agriapp.model.static_data;

import gr.aueb.cf.agriapp.core.enums.CropSeason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Παραμετρικός πίνακας ειδών καλλιέργειας. Η εφαρμογή καλύπτει
 * αποκλειστικά σιτηρά, χειμερινά και εαρινά -- ο χρήστης επιλέγει
 * από τον κατάλογο και δεν εισάγει ελεύθερο κείμενο.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "crop_types")
public class CropType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    /** Επιστημονική ονομασία, π.χ. Triticum durum. */
    private String latinName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropSeason season;
}
