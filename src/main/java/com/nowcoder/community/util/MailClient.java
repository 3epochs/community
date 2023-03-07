package com.nowcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component
public class MailClient {
    private static Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public void sendMail(String recvEmailAddr, String subject, String content) {
        MimeMessage msg = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg);
            helper.setFrom(sender);
            helper.setTo(recvEmailAddr);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(helper.getMimeMessage());
        } catch(Exception e) {
            logger.error("send email failed: " + e.getMessage());
        }

    }
}
