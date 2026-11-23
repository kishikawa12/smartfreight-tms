package com.smartfreight.tmsapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ShipmentCreateDto(
        @NotBlank String origin,
        @NotBlank String destination,
        @Positive double weightKg,
        String carrierId,
        Double quotedPrice,
        String customerName,
        String notes
) {}
