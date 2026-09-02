package gr.aueb.cf.agriapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CropInsertDTO(

        @NotNull(message = "Parcel is required")
        String parcelUuid,

        @NotNull(message = "Crop type is required")
        Long cropTypeId,

        String variety,

        @NotNull(message = "Cultivation year is required")
        @Min(value = 2000, message = "Cultivation year is out of range")
        @Max(value = 2100, message = "Cultivation year is out of range")
        Integer cultivationYear,

        LocalDate plantingDate,

        LocalDate expectedHarvestDate
) {}
