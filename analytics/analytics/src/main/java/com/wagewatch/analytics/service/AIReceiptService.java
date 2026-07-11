package com.wagewatch.analytics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class AIReceiptService {

    @Autowired
    private RestTemplate restTemplate;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 1000;

    public String parseReceipt(byte[] imageBytes, boolean isRaining, String platform) {
        if (!"swiggy".equalsIgnoreCase(platform)) {
            throw new IllegalArgumentException("Testing phase constraint: Only 'Swiggy' platform is supported currently.");
        }

        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API Key is missing from environment variables.");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        String requestBody = """
        {
          "contents": [
            {
              "parts": [
                {
                  "text": "Analyze this delivery receipt layout. Extract exactly: 1. Total payment amount credited to the driver. 2. Forward delivery distance leg in kilometers (ignore backward/return distances entirely). 3. The forward distance rate per kilometer applied (e.g. 10.00). Return ONLY a raw JSON object with exactly three keys: 'platformPay' (decimal), 'platformDistanceKm' (decimal), and 'extractedRatePerKm' (decimal). Do not use text wrappers or markdown tags."
                },
                {
                  "inline_data": {
                    "mime_type": "image/jpeg",
                    "data": "%s"
                  }
                }
              ]
            }
          ]
        }
        """.formatted(base64Image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey.trim());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        int attempt = 0;
        long backoffDelay = INITIAL_BACKOFF_MS;

        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());

                String aiResponseText = root.path("candidates").get(0)
                        .path("content")
                        .path("parts").get(0)
                        .path("text").asText();

                aiResponseText = aiResponseText.replace("```json", "").replace("```", "").trim();
                JsonNode extractedData = mapper.readTree(aiResponseText);

                double platformPay = extractedData.path("platformPay").asDouble(0.0);
                double platformDistanceKm = extractedData.path("platformDistanceKm").asDouble(0.0);
                double extractedRatePerKm = extractedData.path("extractedRatePerKm").asDouble(0.0);

                return buildJsonResponse(platform, platformDistanceKm, isRaining, platformPay, extractedRatePerKm, false);

            } catch (HttpServerErrorException.ServiceUnavailable | HttpServerErrorException.GatewayTimeout e) {
                if (attempt >= MAX_RETRIES) break;
                try {
                    Thread.sleep(backoffDelay);
                    backoffDelay *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }

        // FALLBACK: Allow UI to stay alive if Gemini API fails
        return buildJsonResponse(platform, 0.0, isRaining, 0.0, 10.0, true);
    }

    private String buildJsonResponse(String platform, double distance, boolean isRaining, double pay, double rate, boolean isFailed) {
        return "{\n" +
                "  \"platform\": \"" + platform.toLowerCase() + "\",\n" +
                "  \"platformDistanceKm\": " + (distance == 0.0 ? "\"\"" : distance) + ",\n" +
                "  \"isRaining\": " + isRaining + ",\n" +
                "  \"platformPay\": " + (pay == 0.0 ? "\"\"" : pay) + ",\n" +
                "  \"extractedRatePerKm\": " + rate + ",\n" +
                "  \"isAiExtractionFailed\": " + isFailed + "\n" +
                "}";
    }
}