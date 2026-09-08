package gr.aueb.cf.agriapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CropUpdateDTO(

        @NotNull(message = "Το πεδίο id είναι υποχρεωτικό")
        Long id,

        @NotNull(message = "Το πεδίο uuid είναι υποχρεωτικό")
        String uuid,

        @NotNull(message = "Το είδος καλλιέργειας είναι υποχρεωτικό")
        Long cropTypeId,

        @Size(max = 50, message = "Η ποικιλία δεν μπορεί να ξεπερνά τους 50 χαρακτήρες")
        String variety,

        @NotNull(message = "Η καλλιεργητική περίοδος είναι υποχρεωτική")
        @Min(value = 2000, message = "Η καλλιεργητική περίοδος είναι εκτός εύρους")
        @Max(value = 2100, message = "Η καλλιεργητική περίοδος είναι εκτός εύρους")
        Integer cultivationYear,

        LocalDate plantingDate,

        LocalDate expectedHarvestDate
) {}
