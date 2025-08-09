package com.ddlwlrma.ddlwlrmaaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyKeywordEnricher {
    @Resource
    private ChatModel openAiChatModel;

    /**
     * 把输入的文档列表都补充元信息（打关键词excerpt_keywords）
     * @param documents
     * @return
     */
    public List<Document> enrichDocument(List<Document> documents) {
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(openAiChatModel, 5);
        return keywordMetadataEnricher.apply(documents);
    }
}
