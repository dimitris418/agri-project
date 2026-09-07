package gr.aueb.cf.agriapp.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ParcelReadOnlyDTO(
        Long id,
        String uuid,
        String name,
        RegionalUnitReadOnlyDTO regionalUnitReadOnlyDTO,
        BigDecimal areaInStremmas,
        String kaek,
        Boolean isActive
) {}
