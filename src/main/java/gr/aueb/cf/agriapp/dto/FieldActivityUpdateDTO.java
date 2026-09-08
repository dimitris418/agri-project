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

        @NotNull(message = "Το πεδίο id είναι υποχρεωτικό")
        Long id,

        @NotNull(message = "Το πεδίο uuid είναι υποχρεωτικό")
        String uuid,

        @NotNull(message = "Η ημερομηνία εργασίας είναι υποχρεωτική")
        @PastOrPresent(message = "Η ημερομηνία εργασίας δεν μπορεί να είναι μελλοντική")
        LocalDate activityDate,

        @NotNull(message = "Ο τύπος εργασίας είναι υποχρεωτικός")
        ActivityType type,

        Long productId,

        @DecimalMin(value = "0.00", message = "Η ποσότητα δεν μπορεί να είναι αρνητική")
        BigDecimal quantity,

        UnitOfMeasure unit,

        Long pestId,

        SeverityLevel severity,

        @Size(max = 500, message = "Οι παρατηρήσεις δεν μπορούν να ξεπερνούν τους 500 χαρακτήρες")
        String notes
) {}
