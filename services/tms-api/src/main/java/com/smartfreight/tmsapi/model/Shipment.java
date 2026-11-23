package com.smartfreight.tmsapi.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String reference;

    @Column(nullable = false)
    private String status = "quoted";

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    private double weightKg;

    // plain FK string — no @ManyToOne needed; carrier details not embedded in shipment responses
    @Column(name = "carrier_id")
    private String carrierId;

    private Double quotedPrice;

    @Column(nullable = false)
    private String currency = "USD";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt;

    private String customerName;
    private String notes;

    @OneToMany(mappedBy = "shipment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("timestamp ASC")
    private List<TrackingEvent> trackingEvents = new ArrayList<>();

    protected Shipment() {}

    public Shipment(String reference, String status, String origin, String destination,
                    double weightKg, String carrierId, Double quotedPrice,
                    String customerName, LocalDateTime createdAt) {
        this.reference = reference;
        this.status = status;
        this.origin = origin;
        this.destination = destination;
        this.weightKg = weightKg;
        this.carrierId = carrierId;
        this.quotedPrice = quotedPrice;
        this.customerName = customerName;
        this.createdAt = createdAt;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (statusUpdatedAt == null) statusUpdatedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String           getId()             { return id; }
    public String           getReference()      { return reference; }
    public String           getStatus()         { return status; }
    public String           getOrigin()         { return origin; }
    public String           getDestination()    { return destination; }
    public double           getWeightKg()       { return weightKg; }
    public String           getCarrierId()      { return carrierId; }
    public Double           getQuotedPrice()    { return quotedPrice; }
    public String           getCurrency()       { return currency; }
    public LocalDateTime    getCreatedAt()         { return createdAt; }
    public LocalDateTime    getUpdatedAt()         { return updatedAt; }
    public LocalDateTime    getStatusUpdatedAt()   { return statusUpdatedAt; }
    public String           getCustomerName()   { return customerName; }
    public String           getNotes()          { return notes; }
    public List<TrackingEvent> getTrackingEvents() { return trackingEvents; }

    public void setReference(String reference)        { this.reference = reference; }
    public void setStatus(String status)              { this.status = status; this.statusUpdatedAt = LocalDateTime.now(); }
    public void setStatusUpdatedAt(LocalDateTime t)   { this.statusUpdatedAt = t; }
    public void setOrigin(String origin)              { this.origin = origin; }
    public void setDestination(String destination)    { this.destination = destination; }
    public void setWeightKg(double weightKg)          { this.weightKg = weightKg; }
    public void setCarrierId(String carrierId)        { this.carrierId = carrierId; }
    public void setQuotedPrice(Double quotedPrice)    { this.quotedPrice = quotedPrice; }
    public void setCustomerName(String customerName)  { this.customerName = customerName; }
    public void setNotes(String notes)                { this.notes = notes; }
}
