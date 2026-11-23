package com.smartfreight.tmsapi.controller;

import com.smartfreight.tmsapi.dto.ShipmentCreateDto;
import com.smartfreight.tmsapi.dto.ShipmentDetailDto;
import com.smartfreight.tmsapi.dto.ShipmentDto;
import com.smartfreight.tmsapi.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    /** Clients identify themselves with this header; the web-beta client gets the estimated-delivery preview. */
    static final String CLIENT_HEADER = "X-SmartFreight-Client";
    static final String BETA_CLIENT = "web-beta";

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public List<ShipmentDto> listShipments(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "5") int limit) {
        return shipmentService.listShipments(status, limit);
    }

    @GetMapping("/{shipmentId}")
    public ShipmentDetailDto getShipment(
            @PathVariable String shipmentId,
            @RequestHeader(name = CLIENT_HEADER, required = false) String client) {
        return shipmentService.getShipmentDetail(shipmentId, BETA_CLIENT.equals(client));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentDto createShipment(@Valid @RequestBody ShipmentCreateDto data) {
        return shipmentService.createShipment(data);
    }
}
