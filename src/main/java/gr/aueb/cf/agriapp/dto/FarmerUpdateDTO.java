package gr.aueb.cf.agriapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record FarmerUpdateDTO(

        @NotNull(message = "Το πεδίο id είναι υποχρεωτικό")
        Long id,

        @NotNull(message = "Το πεδίο uuid είναι υποχρεωτικό")
        String uuid,

        @Pattern(regexp = "\\d{0,20}", message = "Ο αριθμός μητρώου πρέπει να περιέχει μόνο ψηφία")
        String registryNumber,

        @Pattern(regexp = "^$|^\\d{10}$", message = "Το τηλέφωνο πρέπει να είναι δέκα ψηφία")
        String phone,

        @NotNull(message = "Το πεδίο isActive είναι υποχρεωτικό")
        Boolean isActive,

        @Valid
        @NotNull(message = "Τα στοιχεία του χρήστη είναι υποχρεωτικά")
        UserUpdateDTO userUpdateDTO
) {}
