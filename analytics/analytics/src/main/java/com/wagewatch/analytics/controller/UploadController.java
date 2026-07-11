package com.wagewatch.analytics.controller;

import com.wagewatch.analytics.service.AIReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload") // Or whatever base mapping your UploadController already uses
@CrossOrigin(origins = "http://localhost:5173")
public class UploadController {

    @Autowired
    private AIReceiptService aiReceiptService;

    // Paste this specific method into your existing UploadController
    @PostMapping("/scan")
    public ResponseEntity<String> scanReceipt(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isRaining", defaultValue = "false") boolean isRaining,
            @RequestParam(value = "platform", defaultValue = "Swiggy") String platform) {

        try {
            byte[] imageBytes = file.getBytes();
            String jsonResult = aiReceiptService.parseReceipt(imageBytes, isRaining, platform);
            return ResponseEntity.ok(jsonResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Failed to process file workflow\"}");
        }
    }

    // ... your other existing UploadController methods ...
}