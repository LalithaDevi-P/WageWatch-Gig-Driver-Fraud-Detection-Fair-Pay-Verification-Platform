package com.wagewatch.analytics.controller;

import com.wagewatch.analytics.dto.DriverRegistrationDTO;
import com.wagewatch.analytics.model.Driver;
import com.wagewatch.analytics.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private DriverRepository driverRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerDriver(@Valid @RequestBody DriverRegistrationDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        if (driverRepository.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("email", "Email is already in use"));
        }
        if (driverRepository.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("phoneNumber", "Phone number is already in use"));
        }

        Driver newDriver = new Driver();
        newDriver.setName(dto.getName());
        newDriver.setEmail(dto.getEmail());
        newDriver.setPhoneNumber(dto.getPhoneNumber());
        newDriver.setPassword(dto.getPassword());
        driverRepository.save(newDriver);

        return ResponseEntity.ok(Map.of("message", "Driver registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginDriver(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        Optional<Driver> driver = driverRepository.findByEmail(email);

        if (driver.isPresent() && driver.get().getPassword().equals(password)) {
            return ResponseEntity.ok(Map.of("message", "Login successful", "email", email));
        }

        return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
    }
}