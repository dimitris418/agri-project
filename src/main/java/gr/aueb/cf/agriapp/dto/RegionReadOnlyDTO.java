package gr.aueb.cf.agriapp.dto;

import lombok.Builder;

@Builder
public record RegionReadOnlyDTO(
        Long id,
        String name
) {}
