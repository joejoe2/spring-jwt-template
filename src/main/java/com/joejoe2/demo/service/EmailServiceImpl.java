package com.joejoe2.demo.service;

import com.joejoe2.demo.exception.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService{
    @Autowired
    private JavaMailSender emailSender;

    @Async
    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        if (to==null||subject==null||text==null)throw new ValidationError("to, subject, or text cannot be null !");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@joejoe2.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            emailSender.send(message);
        }catch (Exception e){
            //this may never occur
            e.printStackTrace();
        }
    }
}
