package gr.aueb.cf.agriapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FarmerStatusUpdateDTO(

        @NotNull(message = "Το πεδίο isActive είναι υποχρεωτικό")
        Boolean isActive
) {}
