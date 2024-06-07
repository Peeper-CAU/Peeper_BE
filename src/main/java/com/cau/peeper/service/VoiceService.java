package com.cau.peeper.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;

@Service
public class VoiceService {

    private static final String AI_SERVER_HOST = "ai.server.host";
    private static final int AI_SERVER_PORT = 12345;

    public void processAudioFile(byte[] wavData) {
        boolean result = sendWavDataToAIServer(wavData);

        if (result) {
            sendNotificationToFirebase();
        }
    }

    private boolean sendWavDataToAIServer(byte[] wavData) {
        try (Socket socket = new Socket(AI_SERVER_HOST, AI_SERVER_PORT);
             OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            outputStream.write(wavData);
            outputStream.flush();

            String response = reader.readLine();
            return Boolean.parseBoolean(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendNotificationToFirebase() {
        Message message = Message.builder()
                .putData("title", "Phishing Analysis Result")
                .putData("body", "It is suspected to be phishing")
//                .setTopic()
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}

