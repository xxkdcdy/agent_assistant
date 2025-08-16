package com.ddlwlrma.ddlwlrmaaiagent.app;

import com.ddlwlrma.ddlwlrmaaiagent.advisor.MyLoggerAdvisor;
import com.ddlwlrma.ddlwlrmaaiagent.advisor.MyReReadingAdvisor;
import com.ddlwlrma.ddlwlrmaaiagent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class TurtleSoupApp {

    private final ChatClient chatClient;
    private final String SYSTEM_PROMPT_SOUP;
    private final String SYSTEM_PROMPT;
    private final Random random = new Random();

    // 定义工具（注册当前类中所有 @Tool 注解的方法）
    private ToolCallback[] soupTools = ToolCallbacks.from(this);

    public TurtleSoupApp(ChatModel openAiChatModel,
                         @Value("classpath:/prompts/system-message-soup-app.st") Resource systemResource,
                         @Value("classpath:/prompts/system-message-soup.st") Resource soupSystemResource) {

        // 初始化系统提示词
        SYSTEM_PROMPT = loadPrompt(systemResource) + getRandomPuzzle();
        SYSTEM_PROMPT_SOUP = loadPrompt(soupSystemResource);

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

        System.out.println(SYSTEM_PROMPT);
    }

    /**
     * 从 Resource 读取并生成系统提示词
     */
    private String loadPrompt(Resource resource) {
        SystemPromptTemplate template = new SystemPromptTemplate(resource);
        return template.createMessage().getText();
    }

    /**
     * 海龟汤类
     * @param description 汤面
     * @param questions   可能会问到的问题
     * @param answer      汤底
     */
    public record TurtleSoup(String description, String questions, String answer) {}

    /**
     * AI海龟汤生成，结构化输出
     */
    @Tool(description = "生成一个海龟汤，包括汤面、可能的问题和汤底")
    public TurtleSoup generateTurtleSoup() {
        TurtleSoup turtleSoup = chatClient.prompt()
                .system(SYSTEM_PROMPT_SOUP + " 每次对话都要生成一个海龟汤，包含汤面和汤底")
                .user("请你根据要求生成一个海龟汤")
                .call()
                .entity(TurtleSoup.class);

        log.info("turtleSoup: {}", turtleSoup);
        return turtleSoup;
    }

    /**
     * 从 classpath: /puzzles 下随机读取一个 md 文件内容
     * @return 文件内容字符串
     * @throws IOException 文件读取异常
     */
    public String getRandomPuzzle() {
        // 读取文件内容
        try {
            // 获取 resources/puzzles 文件夹
            ClassPathResource resourceFolder = new ClassPathResource("puzzles");

            // 列出所有 md 文件
            List<String> mdFiles = Files.list(resourceFolder.getFile().toPath())
                    .filter(path -> path.toString().endsWith(".md"))
                    .map(path -> path.toString())
                    .toList();

            // 随机选择一个文件
            String randomFile = mdFiles.get(random.nextInt(mdFiles.size()));
            String fileContent = Files.readString(java.nio.file.Path.of(randomFile));
            return fileContent;
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * AI基础对话，支持多轮对话记忆（流式输出）
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                .stream()
                .content();
    }

    /**
     * AI基础对话，支持多轮对话记忆（普通模式）
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                .tools(soupTools)
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getText();
        log.info("chat content: {}", content);
        return content;
    }

}
