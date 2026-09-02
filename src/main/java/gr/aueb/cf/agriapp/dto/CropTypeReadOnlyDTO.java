package gr.aueb.cf.agriapp.dto;

import lombok.Builder;

@Builder
public record CropTypeReadOnlyDTO(
        Long id,
        String name,
        String latinName,
        String season
) {}
