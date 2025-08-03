package com.ddlwlrma.ddlwlrmaaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SpringAiAiInvoke implements CommandLineRunner {
    // 这个接口意味着主入口开始run的时候，扫描所有文件，看到有这个接口实现，就调用这里的run方法
    @Resource
    private ChatModel openAiChatModel;   // spring按名称注入的，所以这个变量名不能变(dashscopeChatModel/openAiChatModel)

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage assistantMeesage = openAiChatModel.call(new Prompt("你好"))
                .getResult()
                .getOutput();
        System.out.println(assistantMeesage.getText());
    }
}
