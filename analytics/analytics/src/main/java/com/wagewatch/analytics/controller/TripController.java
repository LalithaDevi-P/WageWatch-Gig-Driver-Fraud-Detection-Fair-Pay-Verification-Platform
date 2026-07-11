package com.wagewatch.analytics.controller;

import com.wagewatch.analytics.model.Driver;
import com.wagewatch.analytics.model.Trip;
import com.wagewatch.analytics.repository.DriverRepository;
import com.wagewatch.analytics.repository.TripRepository;
import com.wagewatch.analytics.service.WageAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/wage")
@CrossOrigin(origins = "http://localhost:5173")
public class TripController {

    @Autowired
    private WageAnalyticsService wageAnalyticsService;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private TripRepository tripRepository; // Injected to fetch driver history

    @PostMapping("/upload")
    public ResponseEntity<?> saveTrip(
            @RequestHeader("X-Driver-Email") String email,
            @RequestBody Trip tripRequest) {
        try {
            Optional<Driver> driver = driverRepository.findByEmail(email);

            if (driver.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"message\": \"Driver not found\"}");
            }

            tripRequest.setDriver(driver.get());

            System.out.println("Processing Trip Data for: " + email + " | " +
                    tripRequest.getPickupArea() + " to " + tripRequest.getDropoffArea());

            Trip savedTrip = wageAnalyticsService.processAndSaveTrip(tripRequest);

            return ResponseEntity.ok(savedTrip);
        } catch (Exception e) {
            System.err.println("Backend Processing Error: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<Map<String, Object>>> getHeatmapData(
            @RequestParam(value = "filter", required = false, defaultValue = "all") String filter) {
        try {
            List<Map<String, Object>> data = wageAnalyticsService.getSafeHeatmapData(filter);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.err.println("Heatmap Generation Error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // THE FIX: Added the endpoint to fetch trips for a specific driver
    @GetMapping("/history/{email}")
    public ResponseEntity<?> getDriverTrips(@PathVariable String email) {
        try {
            List<Trip> trips = tripRepository.findByDriverEmailOrderByCreatedAtDesc(email);
            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            System.err.println("Error fetching history: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}