package com.ddlwlrma.ddlwlrmaaiagent.agent.model;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class MessageSender {

    private final SseEmitter emitter;

    public MessageSender(SseEmitter emitter) {
        this.emitter = emitter;
    }

    private void sendMessage(String prefix, String message) {
        if (emitter != null) {
            try {
                emitter.send(prefix + " " + message);
            } catch (Exception e) {
                // 可以考虑 completeWithError
                e.printStackTrace();
            }
        }
    }

    public void sendThinking(String message) {
        sendMessage("💭 THINKING:", message);
    }

    public void sendStep(String message) {
        sendMessage("⚙️ STEP:", message);
    }

    public void sendTool(String message) {
        sendMessage("🛠 TOOL:", message);
    }

    public void sendResult(String message) {
        sendMessage("📊 RESULT:", message);
    }

    public void sendCompletion(String message) {
        sendMessage("✅ COMPLETION:", message);
    }

    public void sendError(String message) {
        sendMessage("❌ ERROR:", message);
    }
}
