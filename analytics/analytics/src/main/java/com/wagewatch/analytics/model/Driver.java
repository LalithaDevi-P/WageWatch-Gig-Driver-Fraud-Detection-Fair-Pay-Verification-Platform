package com.wagewatch.analytics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "drivers")
public class Driver {

    // The system automatically generates and increments this ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Enforces uniqueness at the database level
    @Column(nullable = false, unique = true)
    private String email;

    // Enforces uniqueness at the database level
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    // We will hash this before saving it in the next step!
    @Column(nullable = false)
    private String password;

    // One Driver can have many Trips
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    @JsonIgnore // Prevents infinite loops when sending JSON back to React
    private List<Trip> trips;

    // Default Constructor
    public Driver() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<Trip> getTrips() { return trips; }
    public void setTrips(List<Trip> trips) { this.trips = trips; }
}