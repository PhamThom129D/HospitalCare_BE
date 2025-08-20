package com.example.hospitalcare_be.controller;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; // Spring annotation
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private static final String API_KEY = "AIzaSyBBY-_LteTZoE6zesGYbrBfwnUteJYyFhE";

    @PostMapping
    public ResponseEntity<String> chatWithGemini(@RequestBody Map<String, String> body) throws IOException {
        String userMessage = body.get("message");

        OkHttpClient client = new OkHttpClient();

        String requestBodyJson = "{ \"contents\": [{\"parts\":[{\"text\":\"" + userMessage + "\"}]}]}";

        // dùng tên đầy đủ okhttp3.RequestBody để tránh trùng
        okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                requestBodyJson,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(GEMINI_API_URL + "?key=" + API_KEY)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            return ResponseEntity.ok(response.body().string());
        }
    }
}
