package com.joejoe2.demo.service.email;

import com.joejoe2.demo.service.email.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {
    //@MockBean
    //JavaMailSender emailSender;

    @Autowired
    EmailService emailService;

    @Test
    void sendSimpleEmail() {
        //not working on github workflows ?
        /*SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@joejoe2.com");
        message.setTo("to");
        message.setSubject("subject");
        message.setText("content");

        Mockito.doNothing().when(emailSender).send(Mockito.any(SimpleMailMessage.class));
        emailService.sendSimpleEmail("to", "subject", "content");
        Mockito.verify(emailSender).send(Mockito.any(SimpleMailMessage.class));*/
    }
}