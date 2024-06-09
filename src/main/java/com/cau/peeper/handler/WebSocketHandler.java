package com.cau.peeper.handler;

import com.cau.peeper.service.VoiceService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    private VoiceService voiceService;

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final String UID_ATTRIBUTE = "uid";

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String uid = message.getPayload();
        sessions.put(uid, session);
        session.getAttributes().put(UID_ATTRIBUTE, uid);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        byte[] payload = message.getPayload().array();
        String uid = (String) session.getAttributes().get(UID_ATTRIBUTE);

        if (uid != null) {
            voiceService.processAudioFile(uid, payload);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws IOException {
        session.close(CloseStatus.SERVER_ERROR);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String uid = (String) session.getAttributes().get(UID_ATTRIBUTE);
        if (uid != null) {
            sessions.remove(uid);
        }
    }
}