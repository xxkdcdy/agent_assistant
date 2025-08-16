package com.ddlwlrma.ddlwlrmaaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// https://github.com/wzk1015/GPT-turtlesoup/blob/main/turtlesoup_prompts.py
// https://github.com/mazzzystar/TurtleBench/blob/main/prompts/prompt_2shots_zh.txt

@SpringBootTest
class TurtleSoupAppTest {

    @Resource
    TurtleSoupApp turtleSoupApp;

    @Test
    void doChatWithSoup() {
        TurtleSoupApp.TurtleSoup turtleSoup = turtleSoupApp.generateTurtleSoup();
        Assertions.assertNotNull(turtleSoup);
    }

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "请生成一个海龟汤";
        String response = turtleSoupApp.doChat(message, chatId);
        Assertions.assertNotNull(response);
        // 第二轮
        message = "故事里有穿越元素吗?";
        response = turtleSoupApp.doChat(message, chatId);
        Assertions.assertNotNull(response);
    }
}