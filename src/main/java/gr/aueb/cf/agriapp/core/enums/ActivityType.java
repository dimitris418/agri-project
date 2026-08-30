package gr.aueb.cf.agriapp.core.enums;

/**
 * Τύπος εργασίας αγρού. Καθορίζει ποια πεδία του FieldActivity
 * είναι υποχρεωτικά -- ο έλεγχος γίνεται στο service layer.
 */
public enum ActivityType {
    SPRAYING,       // Ψεκασμός
    FERTILIZATION,  // Λίπανση
    IRRIGATION,     // Άρδευση
    OBSERVATION,    // Παρατήρηση εχθρού / ασθένειας
    HARVEST         // Συγκομιδή
}
