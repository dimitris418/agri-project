package gr.aueb.cf.agriapp.dto;

import gr.aueb.cf.agriapp.core.enums.ActivityType;
import gr.aueb.cf.agriapp.core.enums.SeverityLevel;
import gr.aueb.cf.agriapp.core.enums.UnitOfMeasure;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record FieldActivityUpdateDTO(

        @NotNull(message = "id field is required")
        Long id,

        @NotNull(message = "uuid field is required")
        String uuid,

        @NotNull(message = "Activity date is required")
        @PastOrPresent(message = "Activity date cannot be in the future")
        LocalDate activityDate,

        @NotNull(message = "Activity type is required")
        ActivityType type,

        Long productId,

        @DecimalMin(value = "0.00", message = "Quantity cannot be negative")
        BigDecimal quantity,

        UnitOfMeasure unit,

        Long pestId,

        SeverityLevel severity,

        @Size(max = 500, message = "Notes cannot exceed 500 characters")
        String notes
) {}
