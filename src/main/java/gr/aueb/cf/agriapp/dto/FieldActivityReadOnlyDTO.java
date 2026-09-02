package gr.aueb.cf.agriapp.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record FieldActivityReadOnlyDTO(
        Long id,
        String uuid,
        LocalDate activityDate,
        String type,
        ProductReadOnlyDTO productReadOnlyDTO,
        BigDecimal quantity,
        String unit,
        PestReadOnlyDTO pestReadOnlyDTO,
        String severity,
        String notes
) {}
