package gr.aueb.cf.agriapp.dto;

import lombok.Builder;

@Builder
public record UserReadOnlyDTO(
        Long id,
        String firstname,
        String lastname,
        String username,
        String vat,
        String role
) {}
