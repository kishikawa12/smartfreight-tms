package com.smartfreight.tmsapi.service;

import com.smartfreight.tmsapi.dto.ShipmentCreateDto;
import com.smartfreight.tmsapi.dto.ShipmentDto;
import com.smartfreight.tmsapi.model.Shipment;
import com.smartfreight.tmsapi.repository.CarrierRepository;
import com.smartfreight.tmsapi.repository.RouteRepository;
import com.smartfreight.tmsapi.repository.ShipmentRepository;
import com.smartfreight.tmsapi.repository.TrackingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A reference collision surfaces when the transaction commits, so the commit is what the tests make fail;
 * `existsByReference` tells the service whether the reference was the cause.
 */
class ShipmentServiceReferenceTest {

    private static final ShipmentCreateDto QUOTE =
            new ShipmentCreateDto("Tokyo", "Detroit", 1000, null, 5418.0, null, null);

    private ShipmentRepository shipments;
    private PlatformTransactionManager transactions;
    private ShipmentService service;

    @BeforeEach
    void setUp() {
        shipments = mock(ShipmentRepository.class);
        transactions = mock(PlatformTransactionManager.class);
        service = new ShipmentService(shipments, mock(CarrierRepository.class), mock(RouteRepository.class),
                mock(TrackingEventRepository.class), transactions);
    }

    @Test
    void referenceIsTheYearAndEightHexCharacters() {
        assertTrue(ShipmentService.newReference().matches("SF-\\d{4}-[0-9A-F]{8}"));
    }

    @Test
    void aTakenReferenceIsRetriedWithANewOne() {
        doThrow(new DataIntegrityViolationException("duplicate key")).doNothing().when(transactions).commit(any());
        when(shipments.existsByReference(anyString())).thenReturn(true);

        ShipmentDto created = service.createShipment(QUOTE);

        ArgumentCaptor<Shipment> saved = ArgumentCaptor.forClass(Shipment.class);
        verify(shipments, times(2)).save(saved.capture());
        assertNotEquals(saved.getAllValues().get(0).getReference(), saved.getAllValues().get(1).getReference());
        assertEquals(saved.getAllValues().get(1).getReference(), created.reference());
    }

    @Test
    void anIntegrityErrorNotCausedByTheReferenceIsNotRetried() {
        doThrow(new DataIntegrityViolationException("null value in column")).when(transactions).commit(any());
        when(shipments.existsByReference(anyString())).thenReturn(false);

        assertThrows(DataIntegrityViolationException.class, () -> service.createShipment(QUOTE));

        verify(shipments, times(1)).save(any());
    }

    @Test
    void collisionsAreGivenUpOnAfterTheAttemptLimit() {
        doThrow(new DataIntegrityViolationException("duplicate key")).when(transactions).commit(any());
        when(shipments.existsByReference(anyString())).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.createShipment(QUOTE));

        verify(shipments, times(ShipmentService.REFERENCE_ATTEMPTS)).save(any());
    }

    @Test
    void aFirstTryThatWorksSavesOnce() {
        doNothing().when(transactions).commit(any());

        service.createShipment(QUOTE);

        verify(shipments, times(1)).save(any());
    }
}
