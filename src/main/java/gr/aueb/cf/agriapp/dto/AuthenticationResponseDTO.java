package gr.aueb.cf.agriapp.dto;

public record AuthenticationResponseDTO(
        String firstname,
        String lastname,
        String token
) {}
