package gr.aueb.cf.agriapp.dto;

import jakarta.validation.constraints.NotEmpty;

public record AuthenticationRequestDTO(

        @NotEmpty(message = "Username is required")
        String username,

        @NotEmpty(message = "Password is required")
        String password
) {}
