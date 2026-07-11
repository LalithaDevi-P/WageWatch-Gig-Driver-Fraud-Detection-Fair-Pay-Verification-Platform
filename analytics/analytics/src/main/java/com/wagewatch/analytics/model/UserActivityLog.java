package com.wagewatch.analytics.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "user_activity_logs")
public class UserActivityLog {

    // We use a hashed string (like a scrambled password) instead of an auto-ID.
    // This ensures we never save their actual phone number or username.
    @Id
    private String hashedDriverId;

    private LocalDate lastUploadDate;

    // --- Constructors ---
    public UserActivityLog() {}

    // --- Getters and Setters ---
    public String getHashedDriverId() { return hashedDriverId; }
    public void setHashedDriverId(String hashedDriverId) { this.hashedDriverId = hashedDriverId; }

    public LocalDate getLastUploadDate() { return lastUploadDate; }
    public void setLastUploadDate(LocalDate lastUploadDate) { this.lastUploadDate = lastUploadDate; }
}