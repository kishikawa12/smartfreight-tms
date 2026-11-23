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

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    private double distanceKm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id")
    private Carrier carrier;

    private double estimatedHours;
    private double carbonKgPerTon;

    public Route() {}

    public String  getId()             { return id; }
    public String  getOrigin()         { return origin; }
    public String  getDestination()    { return destination; }
    public double  getDistanceKm()     { return distanceKm; }
    public Carrier getCarrier()        { return carrier; }
    public double  getEstimatedHours() { return estimatedHours; }
    public double  getCarbonKgPerTon() { return carbonKgPerTon; }

    public void setOrigin(String origin)                { this.origin = origin; }
    public void setDestination(String destination)      { this.destination = destination; }
    public void setDistanceKm(double distanceKm)        { this.distanceKm = distanceKm; }
    public void setCarrier(Carrier carrier)             { this.carrier = carrier; }
    public void setEstimatedHours(double estimatedHours){ this.estimatedHours = estimatedHours; }
    public void setCarbonKgPerTon(double carbonKgPerTon){ this.carbonKgPerTon = carbonKgPerTon; }
}
