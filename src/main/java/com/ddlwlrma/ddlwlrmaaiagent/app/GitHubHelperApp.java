package com.ddlwlrma.ddlwlrmaaiagent.app;

import com.ddlwlrma.ddlwlrmaaiagent.advisor.MyLoggerAdvisor;
import com.ddlwlrma.ddlwlrmaaiagent.advisor.MyReReadingAdvisor;
import com.ddlwlrma.ddlwlrmaaiagent.chatmemory.FileBasedChatMemory;
import com.ddlwlrma.ddlwlrmaaiagent.rag.LoveAppRagCustomAdvisorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class GitHubHelperApp {
    private final ChatClient chatClient;
    private final String SYSTEM_PROMPT;
    @jakarta.annotation.Resource
    private VectorStore pgVectorVectorStore;
    public GitHubHelperApp(ChatModel openAiChatModel,
                         @Value("classpath:/prompts/system-message-github-app.st") Resource systemResource) {

        // 初始化系统提示词
        SYSTEM_PROMPT = loadPrompt(systemResource);

        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        FileBasedChatMemory chatMemory = new FileBasedChatMemory(fileDir);

        // 构建 ChatClient
        chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory), // 对话记忆
                        new MyLoggerAdvisor(),                    // 日志拦截器
                        new MyReReadingAdvisor()                  // re2 增强推理
                )
                .build();

    }

    /**
     * 从 Resource 读取并生成系统提示词
     */
    private String loadPrompt(Resource resource) {
        SystemPromptTemplate template = new SystemPromptTemplate(resource);
        return template.createMessage().getText();
    }

    /**
     * AI基础对话，支持多轮对话记忆（流式输出），带RAG
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1))
                .advisors(
                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor
                                (pgVectorVectorStore, ""))
                .stream()
                .content();
    }

    /**
     * AI基础对话，支持多轮对话记忆（普通模式），带RAG
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                        .advisors(
                            LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor
                                    (pgVectorVectorStore, ""))
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getText();
        log.info("chat content: {}", content);
        return content;
    }
}
