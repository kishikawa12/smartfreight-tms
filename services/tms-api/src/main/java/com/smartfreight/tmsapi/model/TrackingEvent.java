package com.smartfreight.tmsapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_events")
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    // Read-only mirror of shipment_id FK for use in DTOs without triggering lazy load
    @Column(name = "shipment_id", insertable = false, updatable = false)
    private String shipmentId;

    @Column(nullable = false)
    private String status;

    private String location;
    private String description;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    protected TrackingEvent() {}

    public TrackingEvent(Shipment shipment, String status, String location,
                         String description, LocalDateTime timestamp) {
        this.shipment = shipment;
        this.status = status;
        this.location = location;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String        getId()          { return id; }
    public String        getShipmentId()  { return shipmentId; }
    public String        getStatus()      { return status; }
    public String        getLocation()    { return location; }
    public String        getDescription() { return description; }
    public LocalDateTime getTimestamp()   { return timestamp; }
}
