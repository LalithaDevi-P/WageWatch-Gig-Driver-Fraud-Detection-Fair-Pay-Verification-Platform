package com.wagewatch.analytics.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double actualDrivenDistanceKm;
    private double platformDistanceKm;
    private double optimalDistanceKm;

    private double platformPay;

    // Explicitly mapped to prevent null crashes
    @Column(name = "calculated_fair_pay")
    private double calculatedFairPay;

    private double underpaidAmount;
    private double underpaidPercentage;

    // Explicitly mapped to fix the final missing column error
    @Column(name = "is_underpaid")
    private boolean isUnderpaid;

    private String platform;
    private String pickupArea;
    private String dropoffArea;

    // Tell Spring Boot to look for the exact word "isRaining"
    @JsonProperty("isRaining")
    private boolean isRaining;

    private String weatherStatus;

    private double latitude;
    private double longitude;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // ADD THIS ANNOTATION
    @JsonProperty("isUnderpaid")
    public boolean isUnderpaid() {
        return isUnderpaid;
    }

    // ADD THIS ANNOTATION
    @JsonProperty("isUnderpaid")
    public void setUnderpaid(boolean underpaid) {
        this.isUnderpaid = underpaid;
    }
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.weatherStatus == null) {
            this.weatherStatus = this.isRaining ? "RAINY" : "NORMAL";
        }
    }

    // ADD THIS TO Trip.java

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    @JsonIgnore
    private Driver driver;

    // Don't forget to generate the Getter and Setter for this!
    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getActualDrivenDistanceKm() { return actualDrivenDistanceKm; }
    public void setActualDrivenDistanceKm(double actualDrivenDistanceKm) { this.actualDrivenDistanceKm = actualDrivenDistanceKm; }

    public double getPlatformDistanceKm() { return platformDistanceKm; }
    public void setPlatformDistanceKm(double platformDistanceKm) { this.platformDistanceKm = platformDistanceKm; }

    public double getOptimalDistanceKm() { return optimalDistanceKm; }
    public void setOptimalDistanceKm(double optimalDistanceKm) { this.optimalDistanceKm = optimalDistanceKm; }

    public double getPlatformPay() { return platformPay; }
    public void setPlatformPay(double platformPay) { this.platformPay = platformPay; }

    public double getCalculatedFairPay() { return calculatedFairPay; }
    public void setCalculatedFairPay(double calculatedFairPay) { this.calculatedFairPay = calculatedFairPay; }

    public double getUnderpaidAmount() { return underpaidAmount; }
    public void setUnderpaidAmount(double underpaidAmount) { this.underpaidAmount = underpaidAmount; }

    public double getUnderpaidPercentage() { return underpaidPercentage; }
    public void setUnderpaidPercentage(double underpaidPercentage) { this.underpaidPercentage = underpaidPercentage; }



    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getPickupArea() { return pickupArea; }
    public void setPickupArea(String pickupArea) { this.pickupArea = pickupArea; }

    public String getDropoffArea() { return dropoffArea; }
    public void setDropoffArea(String dropoffArea) { this.dropoffArea = dropoffArea; }

    // THE FIX: Force Spring Boot to use the exact JSON key for the Getter
    @JsonProperty("isRaining")
    public boolean getIsRaining() {
        return isRaining;
    }

    // THE FIX: Force Spring Boot to use the exact JSON key for the Setter
    @JsonProperty("isRaining")
    public void setIsRaining(boolean isRaining) {
        this.isRaining = isRaining;
    }

    public String getWeatherStatus() { return weatherStatus; }
    public void setWeatherStatus(String weatherStatus) { this.weatherStatus = weatherStatus; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}