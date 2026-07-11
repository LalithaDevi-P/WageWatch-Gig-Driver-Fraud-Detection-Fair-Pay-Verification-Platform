package com.wagewatch.analytics.repository;

import com.wagewatch.analytics.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    // --- 1. EXISTING METHODS (Used for your City Heatmap) ---
    List<Trip> findByCreatedAtAfter(LocalDateTime timeLimit);

    @Query("SELECT t.pickupArea, t.platform, COUNT(t), AVG(t.underpaidPercentage) " +
            "FROM Trip t " +
            "WHERE t.createdAt >= :timeLimit " +
            "GROUP BY t.pickupArea, t.platform")
    List<Object[]> getAggregatedMetrics(@Param("timeLimit") LocalDateTime timeLimit);


    // --- 2. NEW METHOD (Used for the Personal Dashboard) ---
    // Spring Boot automatically writes the SQL to find trips by the driver's email!
    List<Trip> findByDriverEmailOrderByCreatedAtDesc(String email);

}