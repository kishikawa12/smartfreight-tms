package com.smartfreight.tmsapi.repository;

import com.smartfreight.tmsapi.model.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, String> {

    @Query("SELECT t FROM TrackingEvent t WHERE t.shipment.id = :shipmentId ORDER BY t.timestamp ASC")
    List<TrackingEvent> findByShipmentIdOrdered(@Param("shipmentId") String shipmentId);
}
