package gr.aueb.cf.agriapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ParcelUpdateDTO(

        @NotNull(message = "id field is required")
        Long id,

        @NotNull(message = "uuid field is required")
        String uuid,

        @NotEmpty(message = "Parcel name is required")
        String name,

        String location,

        @NotNull(message = "Area is required")
        @DecimalMin(value = "0.01", message = "Area must be greater than zero")
        BigDecimal areaInStremmas,

        @Pattern(regexp = "^$|^\\d{12}$", message = "KAEK must be a 12-digit number")
        String kaek,

        @NotNull(message = "isActive field is required")
        Boolean isActive
) {}
