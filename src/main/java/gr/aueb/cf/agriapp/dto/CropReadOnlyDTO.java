package gr.aueb.cf.agriapp.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CropReadOnlyDTO(
        Long id,
        String uuid,
        CropTypeReadOnlyDTO cropTypeReadOnlyDTO,
        String variety,
        Integer cultivationYear,
        LocalDate plantingDate,
        LocalDate expectedHarvestDate,
        LocalDate harvestDate
) {}
