package com.cau.peeper.service;

import com.cau.peeper.dto.AnalysisRequest;
import com.cau.peeper.dto.AnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class VoiceService {

    private static final String AI_SERVER_URL = "https://peeper-ai.dev-lr.com/wavAnalysis";
    private final ObjectMapper objectMapper;

    public VoiceService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void processAudioFile(String uid, byte[] wavData) {
        AnalysisRequest analysisRequest = new AnalysisRequest(uid, wavData);
        AnalysisResponse analysisResponse = sendWavDataToAIServer(analysisRequest);

        Boolean messageSending = analysisResponse.getMessageSending();
        String riskLevel = analysisResponse.getRiskLevel();
        if (messageSending) {
            sendNotificationToFirebase(uid, riskLevel);
        }
    }

    private AnalysisResponse sendWavDataToAIServer(AnalysisRequest analysisRequest) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(AI_SERVER_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            String jsonRequest = objectMapper.writeValueAsString(analysisRequest);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(jsonRequest.getBytes());
                outputStream.flush();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response = reader.readLine();
                return objectMapper.readValue(response, AnalysisResponse.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new AnalysisResponse(false, "Error");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void sendNotificationToFirebase(String uid, String riskLevel) {
        Message message = Message.builder()
                .putData("title", "Phishing Analysis Result")
                .putData("body", riskLevel)
                .setTopic(uid)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}

