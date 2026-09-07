package gr.aueb.cf.agriapp.dto;

import lombok.Builder;

@Builder
public record RegionalUnitReadOnlyDTO(
        Long id,
        String name,
        RegionReadOnlyDTO regionReadOnlyDTO
) {}
