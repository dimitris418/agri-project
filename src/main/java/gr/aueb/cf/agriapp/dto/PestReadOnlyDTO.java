package gr.aueb.cf.agriapp.dto;

import lombok.Builder;

@Builder
public record PestReadOnlyDTO(
        Long id,
        String name,
        String latinName,
        String type
) {}
