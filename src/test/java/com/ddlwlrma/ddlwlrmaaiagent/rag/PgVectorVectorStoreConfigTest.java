package com.ddlwlrma.ddlwlrmaaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    VectorStore pgVectorVectorStore;

    @Test
    void testPgVectorVectorStore() {
        List<Document> documents = List.of(
                new Document("编程导航测试测试123", Map.of("meta1", "meta1")),
                new Document("嘿嘿嘿嘿编程哈哈哈哈哈哈"),
                new Document("外比巴卜", Map.of("meta2", "meta2"))
        );
        pgVectorVectorStore.add(documents);
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("编程").topK(3).build());
        Assertions.assertNotNull(results);
    }
}
