package com.ddlwlrma.ddlwlrmaaiagent.rag;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;


public class LoveAppRagCustomAdvisorFactory {
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String filterMetadata) {
        // 过滤特定元信息的文档
//        Filter.Expression expression = new FilterExpressionBuilder()
//                .eq("filename", filterMetadata)
//                .build();

        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
//                .filterExpression(expression)
                .similarityThreshold(0.5)
                .topK(3)
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)     // 为查询增加过滤条件，包括元信息、阈值、召回数量
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())   // query增强器，可以进行异常处理
                .build();
    }
}
