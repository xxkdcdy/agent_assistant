package com.ddlwlrma.ddlwlrmaaiagent.service;

import jakarta.mail.*;
import jakarta.mail.Flags.Flag;
import jakarta.mail.search.FlagTerm;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.SortTerm;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MailService163 {

    public List<String> getUnreadEmailSummaries(String username, String authCode, int maxCount) throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", "imap.qiye.163.com");
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl.enable", "true");

        Session session = Session.getInstance(props);
        Store store = session.getStore("imap");
        store.connect(username, authCode);

        IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] msgs;
        try {
            // 优先用服务器端 SORT（按接收时间 ARRIVAL）
            SortTerm[] sortTerms = {SortTerm.ARRIVAL};
            msgs = inbox.getSortedMessages(sortTerms, new FlagTerm(new Flags(Flag.SEEN), false));

            // getSortedMessages 可能是升序，这里反转为倒序（新→旧）
            reverseArray(msgs);
        } catch (MessagingException ex) {
            // 不支持 SORT 时，本地排序
            Message[] unread = inbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
            Arrays.sort(unread, (m1, m2) -> {
                try {
                    Date d1 = m1.getReceivedDate();
                    Date d2 = m2.getReceivedDate();
                    if (d1 == null && d2 == null) return 0;
                    if (d1 == null) return 1;
                    if (d2 == null) return -1;
                    return d2.compareTo(d1); // 倒序：新在前
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });
            msgs = unread;
        }

        List<String> summaries = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int take = Math.min(msgs.length, maxCount);
        for (int i = 0; i < take; i++) {
            Message msg = msgs[i];

            // 时间
            String time = "";
            try {
                Date date = msg.getReceivedDate();
                if (date != null) time = sdf.format(date);
            } catch (Exception ignored) {}

            // 主题
            String subject;
            try { subject = msg.getSubject(); } catch (Exception e) { subject = ""; }

            // 摘要
            String body = extractText(msg).replaceAll("\\s+", " ");
            if (body.length() > 100) body = body.substring(0, 100);

            summaries.add("时间: " + time + " | 主题: " + subject + " | 摘要: " + body);
        }

        if (summaries.isEmpty()) {
            summaries.add("没有未读邮件。");
        }

        inbox.close(false);
        store.close();
        System.out.println(summaries);
        return summaries;
    }

    // 反转数组（倒序）
    private void reverseArray(Message[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            Message tmp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = tmp;
        }
    }

    // 简单正文提取
    private String extractText(Message message) {
        try {
            Object content = message.getContent();
            if (content instanceof String) {
                return (String) content;
            }
            if (content instanceof Multipart) {
                Multipart mp = (Multipart) content;
                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart bp = mp.getBodyPart(i);
                    Object part = bp.getContent();
                    if (part instanceof String) return (String) part;
                }
            }
        } catch (Exception ignored) {}
        return "";
    }
}
