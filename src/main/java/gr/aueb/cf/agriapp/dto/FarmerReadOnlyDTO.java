package gr.aueb.cf.agriapp.dto;

import lombok.Builder;

@Builder
public record FarmerReadOnlyDTO(
        Long id,
        String uuid,
        String registryNumber,
        String phone,
        Boolean isActive,
        UserReadOnlyDTO userReadOnlyDTO
) {}
