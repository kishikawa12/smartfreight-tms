package com.smartfreight.tmsapi.service;

import com.smartfreight.tmsapi.dto.ShipmentCreateDto;
import com.smartfreight.tmsapi.dto.ShipmentDetailDto;
import com.smartfreight.tmsapi.dto.ShipmentDto;
import com.smartfreight.tmsapi.dto.TrackingEventDto;
import com.smartfreight.tmsapi.model.Carrier;
import com.smartfreight.tmsapi.model.Route;
import com.smartfreight.tmsapi.model.Shipment;
import com.smartfreight.tmsapi.model.TrackingEvent;
import com.smartfreight.tmsapi.repository.CarrierRepository;
import com.smartfreight.tmsapi.repository.RouteRepository;
import com.smartfreight.tmsapi.repository.ShipmentRepository;
import com.smartfreight.tmsapi.repository.TrackingEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    /** Tries per quote before a reference collision is reported as an error. */
    static final int REFERENCE_ATTEMPTS = 5;

    private final ShipmentRepository shipmentRepository;
    private final CarrierRepository carrierRepository;
    private final RouteRepository routeRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final TransactionTemplate transactionTemplate;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           CarrierRepository carrierRepository,
                           RouteRepository routeRepository,
                           TrackingEventRepository trackingEventRepository,
                           PlatformTransactionManager transactionManager) {
        this.shipmentRepository = shipmentRepository;
        this.carrierRepository = carrierRepository;
        this.routeRepository = routeRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public List<ShipmentDto> listShipments(String status, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        List<Shipment> shipments = status != null
                ? shipmentRepository.findByStatusOrderByCreatedAtDesc(status, page)
                : shipmentRepository.findAllOrderByCreatedAtDesc(page);
        return shipments.stream().map(this::toDto).toList();
    }

    public ShipmentDetailDto getShipmentDetail(String shipmentId) {
        // Reference numbers (e.g. SF-2024-001) are never valid UUIDs — skip the id
        // lookup entirely to avoid an internal NoResultException from Hibernate.
        boolean looksLikeReference = shipmentId.matches("SF-\\d{4}-.*");
        Optional<Shipment> found = looksLikeReference
                ? shipmentRepository.findWithTrackingEventsByReference(shipmentId)
                : shipmentRepository.findWithTrackingEventsById(shipmentId)
                        .or(() -> shipmentRepository.findWithTrackingEventsByReference(shipmentId));
        Shipment shipment = found.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found"));
        return toDetailDto(shipment);
    }

    /**
     * Creates a quote, retrying with a new reference if the one drawn is already taken.
     *
     * The unique-key violation is raised when the transaction commits (the id is generated in the
     * application, so nothing is flushed earlier) and the failed transaction cannot be reused. So each
     * attempt runs in a transaction of its own, and this method runs outside one. Only a violation
     * caused by the reference is retried: if no other row holds the reference we drew, the error is
     * something else and is thrown as it was.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ShipmentDto createShipment(ShipmentCreateDto data) {
        for (int attempt = 1; ; attempt++) {
            String reference = newReference();
            try {
                return transactionTemplate.execute(tx -> insertShipment(data, reference));
            } catch (DataIntegrityViolationException e) {
                if (attempt >= REFERENCE_ATTEMPTS || !shipmentRepository.existsByReference(reference)) {
                    throw e;
                }
                log.warn("Shipment reference {} already exists, retrying with a new one (attempt {} of {})",
                        reference, attempt, REFERENCE_ATTEMPTS);
            }
        }
    }

    /** SF-<year>-<8 hex>. Four hex characters (65,536 values a year) were already colliding in about 2% of quotes. */
    public static String newReference() {
        String hex = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "SF-" + LocalDateTime.now().getYear() + "-" + hex;
    }

    private ShipmentDto insertShipment(ShipmentCreateDto data, String reference) {
        String resolvedCarrierId = null;
        if (data.carrierId() != null) {
            // Try UUID → code → name (case-insensitive contains), mirroring Python logic
            Optional<Carrier> found = carrierRepository.findById(data.carrierId())
                    .or(() -> carrierRepository.findByCodeIgnoreCase(data.carrierId()))
                    .or(() -> carrierRepository.findByNameContainingIgnoreCase(data.carrierId()).stream().findFirst());
            if (found.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Carrier '" + data.carrierId() + "' not found. Use a valid carrier ID, code (e.g. FTL), or name.");
            }
            resolvedCarrierId = found.get().getId();
        }

        Double quotedPrice = data.quotedPrice();
        if (quotedPrice == null && resolvedCarrierId != null) {
            // Fall back to calculated price when agent did not provide one
            final String finalCarrierId = resolvedCarrierId;
            Optional<Carrier> carrier = carrierRepository.findById(finalCarrierId);
            if (carrier.isPresent()) {
                List<Route> routes = routeRepository.findAllWithCarrier().stream()
                        .filter(r -> r.getCarrier().getId().equals(finalCarrierId))
                        .limit(1)
                        .toList();
                if (!routes.isEmpty()) {
                    quotedPrice = Math.round(
                            carrier.get().getBaseRatePerKm() * routes.get(0).getDistanceKm()
                            * (data.weightKg() / 1000.0) * 100.0) / 100.0;
                }
            }
        }

        Shipment shipment = new Shipment(reference, "quoted", data.origin(), data.destination(),
                data.weightKg(), resolvedCarrierId, quotedPrice, data.customerName(), null);
        shipment.setNotes(data.notes());
        shipmentRepository.save(shipment);
        return toDto(shipment);
    }

    public List<TrackingEventDto> getTrackingEvents(String shipmentId) {
        return trackingEventRepository.findByShipmentIdOrdered(shipmentId).stream()
                .map(this::toTrackingDto)
                .toList();
    }

    private ShipmentDto toDto(Shipment s) {
        return new ShipmentDto(s.getId(), s.getReference(), s.getStatus(), s.getOrigin(),
                s.getDestination(), s.getWeightKg(), s.getCarrierId(), s.getQuotedPrice(),
                s.getCurrency(), s.getCreatedAt(), s.getUpdatedAt(), s.getCustomerName(), s.getNotes());
    }

    private ShipmentDetailDto toDetailDto(Shipment s) {
        List<TrackingEventDto> events = s.getTrackingEvents().stream()
                .map(this::toTrackingDto)
                .toList();
        return new ShipmentDetailDto(s.getId(), s.getReference(), s.getStatus(), s.getOrigin(),
                s.getDestination(), s.getWeightKg(), s.getCarrierId(), s.getQuotedPrice(),
                s.getCurrency(), s.getCreatedAt(), s.getUpdatedAt(), s.getCustomerName(), s.getNotes(), events);
    }

    private TrackingEventDto toTrackingDto(TrackingEvent t) {
        return new TrackingEventDto(t.getId(), t.getShipmentId(), t.getStatus(),
                t.getLocation(), t.getDescription(), t.getTimestamp());
    }
}
