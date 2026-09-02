package gr.aueb.cf.agriapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record FarmerUpdateDTO(

        @NotNull(message = "id field is required")
        Long id,

        @NotNull(message = "uuid field is required")
        String uuid,

        @Pattern(regexp = "\\d{0,20}", message = "Registry number must be numeric")
        String registryNumber,

        @Pattern(regexp = "^$|^\\d{10}$", message = "Phone must be a 10-digit number")
        String phone,

        @NotNull(message = "isActive field is required")
        Boolean isActive,

        @Valid
        @NotNull(message = "User details are required")
        UserUpdateDTO userUpdateDTO
) {}
