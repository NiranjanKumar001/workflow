package com.nabin.workflow.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String emailFrom;

    @Value("${app.email.from-name}")
    private String emailFromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Send verification email
     */
    @Async
    public void sendVerificationEmail(String to, String username, String token) {
        try {
            log.info("📧 Sending verification email to: {}", to);

            String verificationLink = frontendUrl + "/verify-email?token=" + token;

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("verificationLink", verificationLink);

            String htmlContent = buildVerificationEmail(username, verificationLink);

            sendEmail(to, "Verify Your Email - WorkFlow", htmlContent);

            log.info("✅ Verification email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send password reset email
     */
    @Async
    public void sendPasswordResetEmail(String to, String username, String token) {
        try {
            log.info("📧 Sending password reset email to: {}", to);

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String htmlContent = buildPasswordResetEmail(username, resetLink);

            sendEmail(to, "Reset Your Password - WorkFlow", htmlContent);

            log.info("✅ Password reset email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send email
     */
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(emailFrom, emailFromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
// For user verification with email
    /**
     * Build verification email HTML
     */
    private String buildVerificationEmail(String username, String verificationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; padding: 12px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to WorkFlow!</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s,</h2>
                        <p>Thank you for registering with WorkFlow! We're excited to have you on board.</p>
                        <p>To complete your registration and activate your account, please verify your email address by clicking the button below:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">Verify Email Address</a>
                        </div>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #667eea;">%s</p>
                        <p><strong>This link will expire in 24 hours.</strong></p>
                        <p>If you didn't create an account with WorkFlow, you can safely ignore this email.</p>
                        <p>Best regards,<br>The WorkFlow Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 WorkFlow. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, verificationLink, verificationLink);
    }

    /**
     * Build password reset email HTML
     */
    private String buildPasswordResetEmail(String username, String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; padding: 12px 30px; background: #f5576c; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s,</h2>
                        <p>We received a request to reset your password for your WorkFlow account.</p>
                        <p>Click the button below to reset your password:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </div>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #f5576c;">%s</p>
                        <div class="warning">
                            <p><strong>⚠️ Important:</strong></p>
                            <ul>
                                <li>This link will expire in 1 hour</li>
                                <li>For security, this link can only be used once</li>
                                <li>If you didn't request this, please ignore this email</li>
                            </ul>
                        </div>
                        <p>If you didn't request a password reset, your account is still secure and no changes have been made.</p>
                        <p>Best regards,<br>The WorkFlow Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 WorkFlow. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(username, resetLink, resetLink);
    }
}