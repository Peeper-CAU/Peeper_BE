package com.cau.peeper.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() throws IOException {
        log.info("Initializing Firebase");

        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");

        if (Objects.isNull(serviceAccount)) {
            log.error("Service account is null");
            throw new NullPointerException("service account is null");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        log.info("Firebase initialized successfully");
    }
}

