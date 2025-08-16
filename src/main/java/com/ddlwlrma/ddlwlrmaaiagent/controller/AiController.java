package com.ddlwlrma.ddlwlrmaaiagent.controller;


import com.ddlwlrma.ddlwlrmaaiagent.agent.DdlwlrmaManus;
import com.ddlwlrma.ddlwlrmaaiagent.app.LoveApp;
import com.ddlwlrma.ddlwlrmaaiagent.constant.AiConstant;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel openAiChatModel;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        SseEmitter emitter = new SseEmitter(180000L);

        loveApp.doChatByStream(message, chatId)
                .subscribe(
                        // onNext
                        chunk -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(chunk));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // onError
                        error -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data(error.getMessage()));
                                emitter.send(SseEmitter.event()
                                        .name("complete")
                                        .data(AiConstant.END_CONVERSATION));
                            } catch (IOException ignored) {}
                            emitter.completeWithError(error);
                        },
                        // onComplete
                        () -> {
                            try {
                                // 最后一条标记消息
                                emitter.send(SseEmitter.event()
                                        .name("complete")
                                        .data(AiConstant.END_CONVERSATION));
                            } catch (IOException ignored) {}
                            emitter.complete();
                        }
                );

        return emitter;
    }

    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {

        // 把自定义的tools和MCP提供的tools拼接到一起
        ToolCallback[] allToolsWithMcp = Stream.concat(
                Arrays.stream(allTools),
                Arrays.stream(toolCallbackProvider.getToolCallbacks())
        ).toArray(ToolCallback[]::new);

        DdlwlrmaManus ddlwlrmaManus = new DdlwlrmaManus(allToolsWithMcp, openAiChatModel);
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        return ddlwlrmaManus.runStream(message, emitter);
    }


}
