package com.ddlwlrma.ddlwlrmaaiagent.tools;

import okhttp3.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.time.Duration;

/**
 * 联网搜索工具，使用百度云的API
 */
public class WebSearchTool {
    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder()
            .readTimeout(Duration.ofSeconds(300))
            .build();

    private final String api_key;

    public WebSearchTool(String api_key) {
        this.api_key = api_key;
    }

    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(@ToolParam(description = "search query keyword") String query) {
        MediaType mediaType = MediaType.parse("application/json");

        // 完整的请求体（参考curl示例）
        String requestBody = "{" +
                "\"messages\": [{\"content\": \"" + query + "\", \"role\": \"user\"}]," +
                "\"search_source\": \"baidu_search_v1\"," +
                "\"resource_type_filter\": [" +
                "   {\"type\": \"image\", \"top_k\": 4}," +
                "   {\"type\": \"video\", \"top_k\": 4}," +
                "   {\"type\": \"web\", \"top_k\": 4}" +
                "]," +
                "\"search_recency_filter\": \"year\"," +
                "\"stream\": false," +
                "\"model\": \"ernie-3.5-8k\"," +
                "\"enable_deep_search\": false," +
                "\"enable_followup_query\": false," +
                "\"temperature\": 0.11," +
                "\"top_p\": 0.55," +
                "\"search_mode\": \"auto\"," +
                "\"enable_reasoning\": false" +
                "}";

        RequestBody body = RequestBody.create(requestBody, mediaType);

        Request request = new Request.Builder()
                .url("https://qianfan.baidubce.com/v2/ai_search/chat/completions")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + api_key)
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
//            System.out.println(response.body().string());
            return response.body().string();
        } catch (Exception e) {
            return "error web searching: " + e.getMessage();
        }
    }

    /**
     * 测试函数
     */
//    public static void main(String[] args) throws IOException {
//        MediaType mediaType = MediaType.parse("application/json");
//
//        // 完整的请求体（参考curl示例）
//        String requestBody = "{" +
//                "\"messages\": [{\"content\": \"北京有哪些景点\", \"role\": \"user\"}]," +
//                "\"search_source\": \"baidu_search_v1\"," +
//                "\"resource_type_filter\": [" +
//                "   {\"type\": \"image\", \"top_k\": 4}," +
//                "   {\"type\": \"video\", \"top_k\": 4}," +
//                "   {\"type\": \"web\", \"top_k\": 4}" +
//                "]," +
//                "\"search_recency_filter\": \"year\"," +
//                "\"stream\": false," +
//                "\"model\": \"ernie-3.5-8k\"," +
//                "\"enable_deep_search\": false," +
//                "\"enable_followup_query\": false," +
//                "\"temperature\": 0.11," +
//                "\"top_p\": 0.55," +
//                "\"search_mode\": \"auto\"," +
//                "\"enable_reasoning\": true" +
//                "}";
//
//        RequestBody body = RequestBody.create(requestBody, mediaType);
//
//        Request request = new Request.Builder()
//                .url("https://qianfan.baidubce.com/v2/ai_search/chat/completions")
//                .post(body)
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Authorization", "Bearer bce-v3/ALTAK-9F93aeApFxtVnVBkORPKC/b6e191b9f38c2b4999d35e69edfefe9fe05d6525")
//                .build();
//
//        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
//            System.out.println(response.body().string());
//        }
//    }
}