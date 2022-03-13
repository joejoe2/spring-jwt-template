package com.joejoe2.demo.service;

public interface EmailService {
    /**
     * send email to someone
     * @param to the destination email address
     * @param subject the subject of email
     * @param text the content of email
     */
    void sendSimpleEmail(String to, String subject, String text);
}
