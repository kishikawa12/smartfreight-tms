package com.smartfreight.tmsapi.repository;

import com.smartfreight.tmsapi.model.Shipment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    Optional<Shipment> findByReference(String reference);

    boolean existsByReference(String reference);

    @Query("SELECT s FROM Shipment s ORDER BY s.createdAt DESC")
    List<Shipment> findAllOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT s FROM Shipment s WHERE s.status = :status ORDER BY s.createdAt DESC")
    List<Shipment> findByStatusOrderByCreatedAtDesc(@Param("status") String status, Pageable pageable);

    // Load with tracking events for the detail endpoint
    @Query("SELECT s FROM Shipment s LEFT JOIN FETCH s.trackingEvents WHERE s.id = :id")
    Optional<Shipment> findWithTrackingEventsById(@Param("id") String id);

    @Query("SELECT s FROM Shipment s LEFT JOIN FETCH s.trackingEvents WHERE s.reference = :reference")
    Optional<Shipment> findWithTrackingEventsByReference(@Param("reference") String reference);

    // Lifecycle scheduler queries
    @Query("SELECT s FROM Shipment s WHERE s.status NOT IN :statuses ORDER BY COALESCE(s.statusUpdatedAt, s.createdAt) ASC")
    List<Shipment> findActiveShipments(@Param("statuses") List<String> statuses, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.status NOT IN :statuses")
    long countActiveShipments(@Param("statuses") List<String> statuses);

    @Query("SELECT s FROM Shipment s WHERE s.status = :status AND s.statusUpdatedAt < :cutoff")
    List<Shipment> findByStatusAndUpdatedAtBefore(@Param("status") String status, @Param("cutoff") LocalDateTime cutoff);
}
