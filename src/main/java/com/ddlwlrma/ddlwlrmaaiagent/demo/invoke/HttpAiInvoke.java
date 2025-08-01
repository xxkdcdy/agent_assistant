package com.ddlwlrma.ddlwlrmaaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;

public class HttpAiInvoke {

    // 可从环境变量或配置文件中读取
    private static final String API_KEY = TestApiKey.API_KEY;
    private static final String URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    public static String invoke() {
        // 构造请求 JSON
        JSONObject systemMsg = new JSONObject()
                .putOnce("role", "system")
                .putOnce("content", "You are a helpful assistant.");
        JSONObject userMsg = new JSONObject()
                .putOnce("role", "user")
                .putOnce("content", "你是谁？");

        JSONArray messages = new JSONArray()
                .put(systemMsg)
                .put(userMsg);

        JSONObject input = new JSONObject().putOnce("messages", messages);
        JSONObject parameters = new JSONObject().putOnce("result_format", "message");

        JSONObject body = new JSONObject()
                .putOnce("model", "qwen-plus")
                .putOnce("input", input)
                .putOnce("parameters", parameters);

        // 发起 POST 请求
        try (HttpResponse response = HttpRequest.post(URL)
                .header("Authorization", "Bearer " + API_KEY)
                .contentType(ContentType.JSON.getValue())
                .body(body.toString())
                .execute()) {

            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"调用失败: " + e.getMessage() + "\"}";
        }
    }

    // 测试方法（可选）
    public static void main(String[] args) {
        String result = invoke();
        System.out.println(result);
    }
}
