package gr.aueb.cf.agriapp.dto;

public record ResponseMessageDTO(String code, String description) {

    public ResponseMessageDTO(String code) {
        this(code, "");     // Καλεί τον canonical constructor
    }
}
