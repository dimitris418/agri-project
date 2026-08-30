package gr.aueb.cf.agriapp.model.static_data;

import gr.aueb.cf.agriapp.core.enums.PestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Παραμετρικός κατάλογος εχθρών, ασθενειών και ζιζανίων των σιτηρών.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "pests")
public class Pest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String latinName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PestType type;
}
