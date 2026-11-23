package com.smartfreight.tmsapi.dto;

import java.time.LocalDateTime;

public record ShipmentDto(
        String id,
        String reference,
        String status,
        String origin,
        String destination,
        double weightKg,
        String carrierId,
        Double quotedPrice,
        String currency,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String customerName,
        String notes
) {}
