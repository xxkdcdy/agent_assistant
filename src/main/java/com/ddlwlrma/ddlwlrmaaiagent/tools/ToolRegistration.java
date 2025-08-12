package com.ddlwlrma.ddlwlrmaaiagent.tools;

import com.ddlwlrma.ddlwlrmaaiagent.service.MailService163;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 统一的工具注册类
 */
@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String search_api_key;

    // 从配置文件读取账号和授权码
    @Value("${mail.username}")
    private String username;

    @Value("${mail.auth-code}")
    private String authCode;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        WebSearchTool webSearchTool = new WebSearchTool(search_api_key);
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        MailUnreadSummaryTool mailUnreadSummaryTool = new MailUnreadSummaryTool(new MailService163(), username, authCode);

        return ToolCallbacks.from(
                fileOperationTool,
                webScrapingTool,
                webSearchTool,
                resourceDownloadTool,
                pdfGenerationTool,
                terminalOperationTool,
                mailUnreadSummaryTool
        );
    }
}
