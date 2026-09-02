package gr.aueb.cf.agriapp.dto;

import lombok.Builder;

@Builder
public record ProductReadOnlyDTO(
        Long id,
        String name,
        String activeSubstance,
        String category,
        Integer preHarvestIntervalDays
) {}
