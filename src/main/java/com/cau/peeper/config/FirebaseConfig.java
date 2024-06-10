package com.cau.peeper.config;

import com.google.api.client.util.Value;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.keyPath}")
    private String keyPath;

    @PostConstruct
    public void initialize() throws IOException {
        InputStream serviceAccount = getClass().getResourceAsStream(keyPath);

        if (Objects.isNull(serviceAccount)) {
            throw new NullPointerException("service account is null");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
    }
}

