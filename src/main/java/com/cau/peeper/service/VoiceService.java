package com.cau.peeper.service;

import com.cau.peeper.dto.AnalysisRequest;
import com.cau.peeper.dto.AnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Slf4j
@Service
public class VoiceService {
    private static final String AI_SERVER_URL = "https://peeper-ai.dev-lr.com/wavAnalysis";
    private final ObjectMapper objectMapper;

    public VoiceService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void processAudioFile(String uid, byte[] wavData) {
        log.info("Starting processAudioFile for uid: {}", uid);
        AnalysisRequest analysisRequest = new AnalysisRequest(uid, Base64.getEncoder().encodeToString(wavData));
        AnalysisResponse analysisResponse = sendWavDataToAIServer(analysisRequest);

        Boolean messageSending = analysisResponse.getMessageSending();
        String riskLevel = analysisResponse.getRiskLevel();
        if (messageSending) {
            log.info("Message sending enabled, sending notification to Firebase for uid: {} with risk level: {}", uid, riskLevel);
            sendNotificationToFirebase(uid, riskLevel);
        } else {
            log.info("Message sending disabled for uid: {}", uid);
        }
        log.info("Finished processAudioFile for uid: {}", uid);
    }

    private AnalysisResponse sendWavDataToAIServer(AnalysisRequest analysisRequest) {
        HttpURLConnection connection = null;
        try {
            log.info("Sending WAV data to AI server for uid: {}", analysisRequest.getUid());
            URL url = new URL(AI_SERVER_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            String jsonRequest = objectMapper.writeValueAsString(analysisRequest);
            log.info("JSON Request: {}", jsonRequest);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(jsonRequest.getBytes());
                outputStream.flush();
                log.info("Data sent to AI server for uid: {}", analysisRequest.getUid());
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String response = reader.readLine();
                log.info("Response from AI server: {}", response);
                return objectMapper.readValue(response, AnalysisResponse.class);
            }
        } catch (IOException e) {
            log.error("Error occurred while sending WAV data to AI server: ", e);
            return new AnalysisResponse(false, "Error");
        } finally {
            if (connection != null) {
                connection.disconnect();
                log.info("Disconnected from AI server");
            }
        }
    }

    private void sendNotificationToFirebase(String uid, String riskLevel) {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setBody(riskLevel)
                        .build())
                .setTopic(uid)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
            log.info("Notification sent successfully for uid: {}", uid);
        } catch (FirebaseMessagingException e) {
            log.error("Error occurred while sending notification to Firebase for uid: {}", uid, e);
        }
    }
}

