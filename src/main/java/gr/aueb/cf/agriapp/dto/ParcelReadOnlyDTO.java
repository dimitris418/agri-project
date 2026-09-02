package gr.aueb.cf.agriapp.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ParcelReadOnlyDTO(
        Long id,
        String uuid,
        String name,
        String location,
        BigDecimal areaInStremmas,
        String kaek,
        Boolean isActive
) {}
