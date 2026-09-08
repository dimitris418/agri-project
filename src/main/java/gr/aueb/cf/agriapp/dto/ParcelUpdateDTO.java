package gr.aueb.cf.agriapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ParcelUpdateDTO(

        @NotNull(message = "Το πεδίο id είναι υποχρεωτικό")
        Long id,

        @NotNull(message = "Το πεδίο uuid είναι υποχρεωτικό")
        String uuid,

        @NotEmpty(message = "Η ονομασία του αγροτεμαχίου είναι υποχρεωτική")
        @Size(min = 2, max = 100, message = "Η ονομασία του αγροτεμαχίου πρέπει να έχει από 2 έως 100 χαρακτήρες")
        String name,

        Long regionalUnitId,

        @NotNull(message = "Η έκταση είναι υποχρεωτική")
        @DecimalMin(value = "0.01", message = "Η έκταση πρέπει να είναι μεγαλύτερη από μηδέν")
        BigDecimal areaInStremmas,

        @Pattern(regexp = "^$|^\\d{12}$", message = "Ο ΚΑΕΚ πρέπει να είναι δώδεκα ψηφία")
        String kaek,

        @NotNull(message = "Το πεδίο isActive είναι υποχρεωτικό")
        Boolean isActive
) {}
