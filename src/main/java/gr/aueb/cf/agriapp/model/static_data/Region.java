package gr.aueb.cf.agriapp.model.static_data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Περιφέρεια -- το ανώτερο επίπεδο της διοικητικής διαίρεσης, δεκατρείς
 * συνολικά. Υπάρχει ως ξεχωριστός πίνακας ώστε το όνομά της να μην
 * επαναλαμβάνεται σε κάθε περιφερειακή ενότητα.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "regions")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}
