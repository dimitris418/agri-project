package gr.aueb.cf.agriapp.dto;

import jakarta.validation.constraints.NotEmpty;

public record AuthenticationRequestDTO(

        @NotEmpty(message = "Το όνομα χρήστη είναι υποχρεωτικό")
        String username,

        @NotEmpty(message = "Το συνθηματικό είναι υποχρεωτικό")
        String password
) {}
