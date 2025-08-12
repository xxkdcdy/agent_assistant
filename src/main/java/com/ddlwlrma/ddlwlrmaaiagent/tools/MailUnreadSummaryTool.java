package com.ddlwlrma.ddlwlrmaaiagent.tools;

import com.ddlwlrma.ddlwlrmaaiagent.service.MailService163;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


public class MailUnreadSummaryTool {

    private final MailService163 mailService;

    private final String username;
    private final String authCode;

    public MailUnreadSummaryTool(MailService163 mailService, String username, String authCode) {
        this.mailService = mailService;
        this.username = username;
        this.authCode = authCode;
    }

    @Tool(description = "获取163邮箱未读邮件的摘要")
    public String getUnreadEmails() {
        try {
            List<String> summaries = mailService.getUnreadEmailSummaries(username, authCode, 10);
            return String.join("\n", summaries);
        } catch (Exception e) {
            return "读取未读邮件失败：" + e.getMessage();
        }
    }
}
