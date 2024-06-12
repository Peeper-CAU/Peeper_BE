package com.cau.peeper.handler;

import com.cau.peeper.service.VoiceService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class ServerSocketHandler {

    private ServerSocket serverSocket;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final VoiceService voiceService;

    public ServerSocketHandler(VoiceService voiceService) {
        this.voiceService = voiceService;
    }

    @PostConstruct
    public void startServer() {
        executorService.submit(this::runServer);
    }

    private void runServer() {
        try {
            serverSocket = new ServerSocket(8000);
            log.info("[Server] Client 연결 대기중...");

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                log.info("Client 연결됨.");
                handleClient(socket);
            }
        } catch (IOException e) {
            log.error("Server socket error", e);
        }
    }

    private void handleClient(Socket socket) {
        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 DataInputStream dis = new DataInputStream(socket.getInputStream())) {

                String uid = in.readLine();
                log.info("Received UID: " + uid);

                byte[] buffer = new byte[4096];
                int read;
                while ((read = dis.read(buffer)) != -1) {
                    byte[] wavData = new byte[read];
                    System.arraycopy(buffer, 0, wavData, 0, read);
                    log.info("WAV 데이터 수신 완료: {} bytes", read);

                    voiceService.processAudioFile(uid, wavData);
                }

            } catch (IOException e) {
                log.error("Client handling error", e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.error("Socket close error", e);
                }
            }
        }).start();
    }

    @PreDestroy
    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            executorService.shutdown();
        } catch (IOException e) {
            log.error("Server stop error", e);
        }
    }
}
