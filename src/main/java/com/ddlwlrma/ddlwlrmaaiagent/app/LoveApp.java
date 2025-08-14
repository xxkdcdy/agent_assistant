package com.ddlwlrma.ddlwlrmaaiagent.app;

import com.ddlwlrma.ddlwlrmaaiagent.advisor.MyLoggerAdvisor;
import com.ddlwlrma.ddlwlrmaaiagent.advisor.MyReReadingAdvisor;
import com.ddlwlrma.ddlwlrmaaiagent.chatmemory.FileBasedChatMemory;
import com.ddlwlrma.ddlwlrmaaiagent.rag.LoveAppRagCloudAdvisorConfig;
import com.ddlwlrma.ddlwlrmaaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.ddlwlrma.ddlwlrmaaiagent.rag.PgVectorVectorStoreConfig;
import com.ddlwlrma.ddlwlrmaaiagent.rag.QueryRewriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

// 定义成Bean，并且增加日志
@Component
@Slf4j
public class LoveApp {
    private final ChatClient chatClient;

    private final String SYSTEM_PROMPT;
    @jakarta.annotation.Resource
    private VectorStore loveAppVectorStore;
    // 百炼知识库RAG
//    @jakarta.annotation.Resource
//    private Advisor loveAppRagCloudAdvisor;
//    @jakarta.annotation.Resource
//    private VectorStore pgVectorVectorStore;
    @jakarta.annotation.Resource
    private QueryRewriter queryRewriter;
    @jakarta.annotation.Resource
    private ToolCallback[] allTools;

    @jakarta.annotation.Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * 初始化AI客户端
     * @param openAiChatModel
     */
    public LoveApp(ChatModel openAiChatModel,
                   @Value("classpath:/prompts/system-message.st") Resource systemResource) {
        // 从Resource里面获取SYSTEM PROMPT
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        SYSTEM_PROMPT = systemPromptTemplate.createMessage(Map.of("assistant_name", "知金")).getText();

        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        FileBasedChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于内存的对话记忆
//        ChatMemory chatMemory = new InMemoryChatMemory();

        chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        // 设置好对话ID，不指定就是所有用户在同一个房间
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 日志拦截器
                        new MyLoggerAdvisor(),
                        // 推理增强的re2拦截器
                        new MyReReadingAdvisor()
                )    //默认拦截器，对所有请求生效，在调用client的时候也可以动态设置
                .build();
    }

    /**
     * AI基础对话，支持多轮对话记忆
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI基础对话，支持多轮对话记忆
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

    /**
     * 恋爱报告类
     * @param title
     * @param suggestions
     */
    record LoveReport(String title, List<String> suggestions) {

    }

    /**
     * AI对话报告工程，结构化输出
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient.prompt()
                .system(SYSTEM_PROMPT + "每次对话都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);

        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    public String doChatWithRag(String message, String chatId) {
        // query改写
        String rewrittenQuery = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient.prompt()
                .user(rewrittenQuery)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 基于内存的RAG
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // 基于百炼知识库的RAG
//                .advisors(loveAppRagCloudAdvisor)
                // 基于pgVector的RAG
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
//                .advisors(
//                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor
//                                (pgVectorVectorStore, "恋爱常见问题和回答 - 已婚篇.md"))
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 支持工具调用
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 支持MCP调用
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
