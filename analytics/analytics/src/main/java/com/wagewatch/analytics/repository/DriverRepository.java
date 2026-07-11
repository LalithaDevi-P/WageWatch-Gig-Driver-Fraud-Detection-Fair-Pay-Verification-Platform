package com.wagewatch.analytics.repository;

import com.wagewatch.analytics.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    // Spring Boot writes the SQL for these automatically!
    Optional<Driver> findByEmail(String email);
    Optional<Driver> findByPhoneNumber(String phoneNumber);

}