package com.wagewatch.analytics.service;

import com.wagewatch.analytics.model.Trip;
import com.wagewatch.analytics.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WageAnalyticsService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private GeocodingService geocodingService;

    public Trip processAndSaveTrip(Trip trip) {
        // 1. Resolve coordinates
        double[] pickupCoords = geocodingService.getCoordinates(trip.getPickupArea());
        double[] dropoffCoords = geocodingService.getCoordinates(trip.getDropoffArea());

        trip.setLatitude(pickupCoords[0]); // Anchor heatmap to pickup
        trip.setLongitude(pickupCoords[1]);

        // 2. Fetch the true optimal driving route distance
        double trueOptimalDistance = geocodingService.getDrivingDistance(pickupCoords, dropoffCoords);
        System.out.println("DEBUG: Map Engine calculated true distance as: " + trueOptimalDistance + " km");

        if (trueOptimalDistance <= 0) {
            System.out.println("WARNING: Geocoder returned 0. Using 5.2km fallback.");
            trueOptimalDistance = 5.2;
        }
        trip.setOptimalDistanceKm(trueOptimalDistance);

        // 3. STRICT FRAUD GUARDRAIL: Only allow +/- 1.0 km buffer
        double minAllowed = Math.max(0.1, trueOptimalDistance - 1.0);
        double maxAllowed = trueOptimalDistance + 1.0;

        System.out.println("DEBUG: Driver Input: " + trip.getActualDrivenDistanceKm() + " | Allowed Range: " + minAllowed + " to " + maxAllowed);

        if (trip.getActualDrivenDistanceKm() > maxAllowed || trip.getActualDrivenDistanceKm() < minAllowed) {
            double displayDist = Math.round(trueOptimalDistance * 100.0) / 100.0;
            throw new IllegalArgumentException("Invalid distance. The map verified this route is ~" + displayDist + " km. Your entry was flagged.");
        }

        // 4. DYNAMIC RATE CALCULATION (Replacing the hardcoded ₹10)
        double platformPay = trip.getPlatformPay();
        double platformDistance = trip.getPlatformDistanceKm();

        // Safety Catch: Prevent division by zero if the AI or user enters 0
        if (platformDistance <= 0) {
            platformDistance = 1.0;
        }

        // Find the exact rate per km Swiggy paid for this specific trip
        double exactRatePerKm = platformPay / platformDistance;

        // Calculate what they SHOULD have earned based on the actual distance they drove
        double expectedFairPay = trip.getActualDrivenDistanceKm() * exactRatePerKm;

        // Keep your weather premium logic intact
        if (trip.getIsRaining()) {
            expectedFairPay += 15.0;
        }

        trip.setCalculatedFairPay(Math.round(expectedFairPay * 100.0) / 100.0);

        // 5. Compute Exact Wage Gap Deficits
        double underpaidAmount = Math.max(0.0, expectedFairPay - trip.getPlatformPay());
        trip.setUnderpaidAmount(Math.round(underpaidAmount * 100.0) / 100.0);

        if (expectedFairPay > 0.0) {
            double percentage = (underpaidAmount / expectedFairPay) * 100.0;
            trip.setUnderpaidPercentage(Math.round(percentage * 100.0) / 100.0);
            trip.setUnderpaid(underpaidAmount > 0);
        } else {
            trip.setUnderpaidPercentage(0.0);
            trip.setUnderpaid(false);
        }

        return tripRepository.save(trip);
    }

    // UPDATED: Now filters data dynamically based on temporal selections
    public List<Map<String, Object>> getSafeHeatmapData(String filter) {
        List<Trip> allTrips;
        LocalDateTime now = LocalDateTime.now();

        if (filter == null) {
            filter = "all";
        }

        switch (filter.toLowerCase()) {
            case "24h":
                allTrips = tripRepository.findByCreatedAtAfter(now.minusHours(24));
                break;
            case "7d":
                allTrips = tripRepository.findByCreatedAtAfter(now.minusDays(7));
                break;
            case "30d":
                allTrips = tripRepository.findByCreatedAtAfter(now.minusDays(30));
                break;
            case "all":
            default:
                allTrips = tripRepository.findAll();
                break;
        }

        Map<String, AreaStats> statsMap = new HashMap<>();

        for (Trip trip : allTrips) {
            String rawArea = trip.getPickupArea();
            Double percentageObj = trip.getUnderpaidPercentage();
            Double lat = trip.getLatitude();
            Double lon = trip.getLongitude();

            if (rawArea == null || rawArea.trim().isEmpty() || percentageObj == null || lat == null || lon == null) {
                continue;
            }

            double percentage = percentageObj.doubleValue();
            String cleanedArea = rawArea.trim();
            cleanedArea = cleanedArea.substring(0, 1).toUpperCase() + cleanedArea.substring(1).toLowerCase();

            statsMap.putIfAbsent(cleanedArea, new AreaStats(cleanedArea, lat, lon));
            AreaStats stats = statsMap.get(cleanedArea);
            stats.addTrip(percentage);
        }

        List<Map<String, Object>> heatmapData = new ArrayList<>();
        for (AreaStats stats : statsMap.values()) {
            if (stats.tripCount >= 1) {
                Map<String, Object> areaData = new HashMap<>();
                areaData.put("neighborhood", stats.areaName);
                areaData.put("latitude", stats.lat);
                areaData.put("longitude", stats.lon);
                areaData.put("totalTrips", stats.tripCount);
                areaData.put("avgUnderpaymentPercentage", Math.round((stats.totalPercentage / stats.tripCount) * 100.0) / 100.0);
                heatmapData.add(areaData);
            }
        }
        return heatmapData;
    }

    private static class AreaStats {
        String areaName;
        int tripCount = 0;
        double totalPercentage = 0.0;
        double lat;
        double lon;

        AreaStats(String areaName, double lat, double lon) {
            this.areaName = areaName;
            this.lat = lat;
            this.lon = lon;
        }

        void addTrip(double percentage) {
            this.tripCount++;
            this.totalPercentage += percentage;
        }
    }
}