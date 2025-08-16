package com.ddlwlrma.ddlwlrmaaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.ddlwlrma.ddlwlrmaaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类  
 */  
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {  
  
    // 可用的工具  
    private final ToolCallback[] availableTools;
  
    // 保存了工具调用信息的响应  
    private ChatResponse toolCallChatResponse;
  
    // 工具调用管理者  
    private final ToolCallingManager toolCallingManager;
  
    // 禁用内置的工具调用机制，自己维护上下文  
    private final ChatOptions chatOptions;
  
    public ToolCallAgent(ToolCallback[] availableTools) {  
        super();  
        this.availableTools = availableTools;  
        this.toolCallingManager = ToolCallingManager.builder().build();  
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文  
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)  
                .build();  
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        // 发送思考开始信息
        messageSender.sendStep("正在分析当前情况...");

        // 校验提示词
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }

        // 发送LLM调用信息
        messageSender.sendThinking("正在调用大语言模型进行推理...");

        // 调用LLM，获取结果
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            // 获取带工具选项的响应
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于 Act
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 输出提示信息
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

            // 发送思考结果
            messageSender.sendThinking("分析完成: " + result);
            log.info(getName() + "的思考: " + result);

            if (!toolCallList.isEmpty()) {
                String toolCallInfo = toolCallList.stream()
                        .map(toolCall -> String.format("工具名称：%s，参数：%s",
                                toolCall.name(),
                                toolCall.arguments())
                        )
                        .collect(Collectors.joining("\n"));
                log.info(toolCallInfo);

                // 发送工具选择信息
                messageSender.sendResult("选择了 " + toolCallList.size() + " 个工具: " +
                        toolCallList.stream()
                                .map(AssistantMessage.ToolCall::name)
                                .collect(Collectors.joining(", ")));
            }

            if (toolCallList.isEmpty()) {
                // 只有不调用工具时，才记录助手消息
                getMessageList().add(assistantMessage);
                messageSender.sendResult("无需使用工具，思考完成");
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            getMessageList().add(
                    new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        // 调用工具
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        // 当前工具调用的结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成了它的任务！结果: " + response.responseData())
                .collect(Collectors.joining("\n"));
        // 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        log.info(results);
        return results;

    }

}
