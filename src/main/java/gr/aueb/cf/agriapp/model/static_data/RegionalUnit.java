package gr.aueb.cf.agriapp.model.static_data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Περιφερειακή ενότητα. Είναι το επίπεδο στο οποίο δηλώνεται η τοποθεσία ενός
 * αγροτεμαχίου: η περιφέρεια είναι πολύ ευρεία και ο δήμος πολύ στενός για
 * κατάλογο επιλογής.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "regional_units")
public class RegionalUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
}
