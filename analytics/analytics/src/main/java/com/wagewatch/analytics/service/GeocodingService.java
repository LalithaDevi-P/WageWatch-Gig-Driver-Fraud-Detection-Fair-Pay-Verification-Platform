package com.wagewatch.analytics.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GeocodingService {

    @Autowired
    private RestTemplate restTemplate;

    private final HttpEntity<String> authEntity;

    public GeocodingService() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "WageWatchApp/1.0 (Portfolio Project)");
        this.authEntity = new HttpEntity<>(headers);
    }

    // 1. THE HYBRID GEOCODER
    public double[] getCoordinates(String area) {
        if (area == null || area.trim().isEmpty()) {
            return new double[]{12.2958, 76.6394};
        }

        String cleanArea = area.trim().toLowerCase();

        // STEP A: The Bulletproof Dictionary (Guaranteed Accuracy for known hubs)
        switch (cleanArea) {
            case "gokulam": return new double[]{12.3250, 76.6320}; // Perfect Northwest coordinates
            case "vijayanagar": return new double[]{12.3275, 76.6180};
            case "kuvempunagar": return new double[]{12.2855, 76.6225};
            case "saraswathipuram": return new double[]{12.3025, 76.6300};
            case "jp nagar": return new double[]{12.2700, 76.6500};
            case "hebbal": return new double[]{12.3535, 76.6025};
            case "metagalli industrial area": return new double[]{12.3450, 76.6240};
            case "kyathamaranahalli": return new double[]{12.3160, 76.6660};
            case "bogadi": return new double[]{12.3000, 76.6000};
            case "jayalakshmipuram": return new double[]{12.3125, 76.6355};
        }

        // STEP B: The Dynamic Fallback (For new/unknown areas)
        double[] coords = fetchFromNominatim(cleanArea);
        if (coords != null) {
            return coords;
        }

        System.err.println("WARNING: Geocoding completely failed for: " + area + ". Falling back to center of Mysuru.");
        return new double[]{12.2958, 76.6394};
    }

    private double[] fetchFromNominatim(String searchArea) {
        try {
            // Try with 'Mysore' as it often yields better results in older OSM databases
            String searchQuery = searchArea + ", Mysore, India";
            String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());

            String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedQuery;

            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, authEntity, List.class);
            List<Map> results = response.getBody();

            if (results != null && !results.isEmpty()) {
                double lat = Double.parseDouble(results.get(0).get("lat").toString());
                double lon = Double.parseDouble(results.get(0).get("lon").toString());
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            System.err.println("API Error resolving location for " + searchArea + ": " + e.getMessage());
        }
        return null;
    }

    // 2. The Router (Calculates exact driving distance between two points)
    public double getDrivingDistance(double[] startCoords, double[] endCoords) {
        try {
            String url = String.format("http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
                    startCoords[1], startCoords[0], endCoords[1], endCoords[0]);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, authEntity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("routes")) {
                List<Map<String, Object>> routes = (List<Map<String, Object>>) body.get("routes");
                if (!routes.isEmpty()) {
                    double distanceMeters = Double.parseDouble(routes.get(0).get("distance").toString());
                    return distanceMeters / 1000.0;
                }
            }
        } catch (Exception e) {
            System.err.println("OSRM Routing failed: " + e.getMessage());
        }
        return 5.0;
    }
}