package com.joejoe2.demo.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {
    @MockBean
    JavaMailSender emailSender;

    @Autowired
    EmailService emailService;

    @Test
    void sendSimpleEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@joejoe2.com");
        message.setTo("to");
        message.setSubject("subject");
        message.setText("content");

        Mockito.doNothing().when(emailSender).send(message);
        try {
            emailService.sendSimpleEmail("to", "subject", "content");
        }catch (Exception e){
            System.out.println("we do not care send out or not !");
        }
        Mockito.verify(emailSender).send(message);
    }
}