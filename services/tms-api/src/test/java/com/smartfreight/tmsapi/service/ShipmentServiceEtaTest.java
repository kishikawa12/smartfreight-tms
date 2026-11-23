package com.smartfreight.tmsapi.service;

import com.smartfreight.tmsapi.model.Carrier;
import com.smartfreight.tmsapi.model.Shipment;
import com.smartfreight.tmsapi.repository.CarrierRepository;
import com.smartfreight.tmsapi.repository.RouteRepository;
import com.smartfreight.tmsapi.repository.ShipmentRepository;
import com.smartfreight.tmsapi.repository.TrackingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The estimated-delivery preview on the shipment detail: the creation time plus the carrier's average transit time. */
class ShipmentServiceEtaTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 11, 1, 9, 30);

    private CarrierRepository carriers;
    private ShipmentRepository shipments;
    private ShipmentService service;

    @BeforeEach
    void setUp() {
        carriers = mock(CarrierRepository.class);
        shipments = mock(ShipmentRepository.class);
        service = new ShipmentService(shipments, carriers, mock(RouteRepository.class),
                mock(TrackingEventRepository.class), mock(PlatformTransactionManager.class));

        Carrier carrier = mock(Carrier.class);
        when(carrier.getId()).thenReturn("carrier-1");
        when(carrier.getAvgTransitDays()).thenReturn(2.5);
        when(carriers.findAll()).thenReturn(List.of(carrier));
    }

    private void shipmentWithCarrier(String carrierId) {
        Shipment shipment = new Shipment("SF-2026-0000AAAA", "quoted", "Chicago", "Boston", 500, carrierId, 900.0,
                "Acme", CREATED);
        when(shipments.findWithTrackingEventsById("s1")).thenReturn(Optional.of(shipment));
    }

    @Test
    void thePreviewIsTheCreationTimePlusTheCarriersTransitTime() {
        shipmentWithCarrier("carrier-1");

        assertEquals(CREATED.plusHours(60), service.getShipmentDetail("s1", true).estimatedDelivery());
    }

    @Test
    void withoutThePreviewThereIsNoEstimateAndNoCarrierLookup() {
        shipmentWithCarrier("carrier-1");

        assertNull(service.getShipmentDetail("s1", false).estimatedDelivery());
        verify(carriers, never()).findAll();
    }

    @Test
    void aShipmentWithoutACarrierHasNoEstimateWhenThePreviewIsOff() {
        shipmentWithCarrier(null);

        assertNull(service.getShipmentDetail("s1", false).estimatedDelivery());
    }
}
