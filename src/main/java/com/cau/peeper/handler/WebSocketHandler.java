package com.cau.peeper.handler;

import com.cau.peeper.service.VoiceService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@Component
public class WebSocketHandler extends BinaryWebSocketHandler {

    private VoiceService voiceService;

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        byte[] payload = message.getPayload().array();
        voiceService.processAudioFile(payload);
    }
}