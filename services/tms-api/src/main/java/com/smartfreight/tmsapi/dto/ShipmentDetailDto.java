package com.smartfreight.tmsapi.dto;

import java.time.LocalDateTime;
import java.util.List;

// Mirrors Python ShipmentDetail — all shipment fields plus tracking_events list; estimatedDelivery is set only
// for clients that ask for the ETA preview (see ShipmentController)
public record ShipmentDetailDto(
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
        String notes,
        List<TrackingEventDto> trackingEvents,
        LocalDateTime estimatedDelivery
) {}
