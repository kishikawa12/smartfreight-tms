package com.smartfreight.tmsapi.dto;

import java.time.LocalDateTime;

public record TrackingEventDto(
        String id,
        String shipmentId,
        String status,
        String location,
        String description,
        LocalDateTime timestamp
) {}
