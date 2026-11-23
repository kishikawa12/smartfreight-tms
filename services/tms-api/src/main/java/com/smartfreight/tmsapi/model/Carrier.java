package com.smartfreight.tmsapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "carriers")
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String mode;

    private double baseRatePerKm;
    private double reliabilityScore;
    private double avgTransitDays;

    @Column(nullable = false)
    private int active = 1;

    protected Carrier() {}

    public Carrier(String name, String code, String mode,
                   double baseRatePerKm, double reliabilityScore, double avgTransitDays) {
        this.name = name;
        this.code = code;
        this.mode = mode;
        this.baseRatePerKm = baseRatePerKm;
        this.reliabilityScore = reliabilityScore;
        this.avgTransitDays = avgTransitDays;
        this.active = 1;
    }

    public String getId()               { return id; }
    public String getName()             { return name; }
    public String getCode()             { return code; }
    public String getMode()             { return mode; }
    public double getBaseRatePerKm()    { return baseRatePerKm; }
    public double getReliabilityScore() { return reliabilityScore; }
    public double getAvgTransitDays()   { return avgTransitDays; }
    public int    getActive()           { return active; }
}
