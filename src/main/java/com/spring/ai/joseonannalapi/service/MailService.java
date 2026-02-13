package com.spring.ai.joseonannalapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendTempPassword(String toEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[조선왕조실록] 임시 비밀번호 안내");
        message.setText("""
                안녕하세요. 조선왕조실록 서비스입니다.

                요청하신 임시 비밀번호를 아래와 같이 안내드립니다.

                임시 비밀번호: %s

                로그인 후 반드시 비밀번호를 변경해 주세요.

                감사합니다.
                """.formatted(tempPassword));
        mailSender.send(message);
    }
}
