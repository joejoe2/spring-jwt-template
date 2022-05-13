package com.joejoe2.demo.service.email;

import com.joejoe2.demo.service.email.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {
    /*@MockBean
    JavaMailSender emailSender;*/

    /*@Autowired
    EmailService emailService;*/

    @Test
    void sendSimpleEmail() {
        /*
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@joejoe2.com");
        message.setTo("to");
        message.setSubject("subject");
        message.setText("content");

        Mockito.doNothing().when(emailSender).send(Mockito.any(SimpleMailMessage.class));
        emailService.sendSimpleEmail("to", "subject", "content");
        Mockito.verify(emailSender, Mockito.timeout(3000).times(1)).send(Mockito.any(SimpleMailMessage.class));
        */
    }
}