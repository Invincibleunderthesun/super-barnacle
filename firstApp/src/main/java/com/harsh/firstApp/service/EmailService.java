package com.harsh.firstApp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service for sending notifications.
 * When SMTP is not configured (mail.username is blank), emails are logged
 * to the console so development/testing works without an email provider.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a password reset email.
     * If SMTP is not configured, logs the token to console.
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        String subject = "Password Reset Request";
        String body = String.format("""
                Hi,

                You requested a password reset. Click the link below to set a new password:

                %s

                This link will expire in 30 minutes.

                If you didn't request this, please ignore this email.

                — Your E-Commerce Store
                """, resetLink);

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send an order confirmation email.
     */
    public void sendOrderConfirmationEmail(String toEmail, Long orderId, double totalAmount) {
        String subject = "Order Confirmed — #" + orderId;
        String body = String.format("""
                Hi,

                Your order #%d has been confirmed!

                Total Amount: ₹%.2f

                You can track your order status in your account dashboard.

                Thank you for shopping with us!

                — Your E-Commerce Store
                """, orderId, totalAmount);

        sendEmail(toEmail, subject, body);
    }

    /**
     * Send an order status update email.
     */
    public void sendOrderStatusEmail(String toEmail, Long orderId, String newStatus) {
        String subject = "Order #" + orderId + " — " + newStatus;
        String body = String.format("""
                Hi,

                Your order #%d status has been updated to: %s

                Thank you for your patience!

                — Your E-Commerce Store
                """, orderId, newStatus);

        sendEmail(toEmail, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        if (mailUsername == null || mailUsername.isBlank()) {
            // No SMTP configured — log to console for development
            logger.info("╔══════════════════════════════════════════════════");
            logger.info("║ 📧 EMAIL (logged — SMTP not configured)");
            logger.info("║ To: {}", to);
            logger.info("║ Subject: {}", subject);
            logger.info("║ Body:");
            for (String line : body.split("\n")) {
                logger.info("║   {}", line);
            }
            logger.info("╚══════════════════════════════════════════════════");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("Email sent to {} — subject: {}", to, subject);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
