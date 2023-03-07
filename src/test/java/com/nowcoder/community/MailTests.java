package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "HuXiang");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("hxiang96@foxmail.com",
                "HTML test", content);
    }


    @Test
    public void testTextMail() {
        mailClient.sendMail(
                "hxiang96@foxmail.com",
                "Test Java Send Email", "Text content");
    }

}
