package gr.aueb.cf.agriapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record FarmerInsertDTO(

        @Pattern(regexp = "\\d{0,20}", message = "Ο αριθμός μητρώου πρέπει να περιέχει μόνο ψηφία")
        String registryNumber,

        @Pattern(regexp = "^$|^\\d{10}$", message = "Το τηλέφωνο πρέπει να είναι δέκα ψηφία")
        String phone,

        @Valid
        @NotNull(message = "Τα στοιχεία του χρήστη είναι υποχρεωτικά")
        UserInsertDTO userInsertDTO
) {}
