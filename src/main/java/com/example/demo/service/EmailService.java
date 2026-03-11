package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Async
    public void sendHtmlMail(String to, String subject, String htmlContent)
            throws MessagingException {

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML

        javaMailSender.send(message);
    }
    public String buildForgotPasswordEmailTemplate(String otpCode, String email) {
        String template = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Reset Your Password</title>
        </head>
        <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5;">
            <table role="presentation" style="width: 100%%; border-collapse: collapse; background-color: #f5f5f5;">
                <tr>
                    <td align="center" style="padding: 40px 0;">
                        <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
        
                            <!-- Header -->
                            <tr>
                                <td style="padding: 40px 40px 30px; text-align: center; border-bottom: 1px solid #e5e5e5;">
                                    <div style="width: 48px; height: 48px; margin: 0 auto 20px; background-color: #ef4444; border-radius: 50%%; display: flex; align-items: center; justify-content: center;">
                                        <span style="font-size: 24px; color: #ffffff;">🔒</span>
                                    </div>
                                    <h1 style="margin: 0; color: #1a1a1a; font-size: 28px; font-weight: 600;">
                                        Reset Your Password
                                    </h1>
                                </td>
                            </tr>
        
                            <!-- Content -->
                            <tr>
                                <td style="padding: 40px;">
                                    <p style="margin: 0 0 20px; color: #4a4a4a; font-size: 16px; line-height: 24px;">
                                        Hello,
                                    </p>
                                    <p style="margin: 0 0 30px; color: #4a4a4a; font-size: 16px; line-height: 24px;">
                                        We received a request to reset the password for your account associated with <strong>{{EMAIL}}</strong>. 
                                        Please use the verification code below to proceed with resetting your password:
                                    </p>
                                    
                                    <!-- OTP Box -->
                                    <table role="presentation" style="width: 100%%; border-collapse: collapse; margin: 30px 0;">
                                        <tr>
                                            <td align="center" style="padding: 30px; background-color: #fef2f2; border-radius: 8px; border: 2px dashed #fca5a5;">
                                                <div style="font-size: 42px; font-weight: 700; letter-spacing: 8px; color: #dc2626; font-family: 'Courier New', monospace;">
                                                    {{OTP}}
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                    
                                    <!-- Warning Box -->
                                    <table role="presentation" style="width: 100%%; border-collapse: collapse; margin: 30px 0;">
                                        <tr>
                                            <td style="padding: 20px; background-color: #fef3c7; border-left: 4px solid #f59e0b; border-radius: 4px;">
                                                <p style="margin: 0; color: #92400e; font-size: 14px; line-height: 20px;">
                                                    ⚠️ <strong>Important:</strong> This verification code is valid for <strong>90 seconds</strong>. 
                                                    Never share this code with anyone, including our support team.
                                                </p>
                                            </td>
                                        </tr>
                                    </table>
                                    
                                    <!-- Security Notice -->
                                    <table role="presentation" style="width: 100%%; border-collapse: collapse; margin: 30px 0;">
                                        <tr>
                                            <td style="padding: 20px; background-color: #f0f9ff; border-left: 4px solid #3b82f6; border-radius: 4px;">
                                                <p style="margin: 0 0 10px; color: #1e40af; font-size: 14px; line-height: 20px; font-weight: 600;">
                                                    Didn't request this?
                                                </p>
                                                <p style="margin: 0; color: #1e3a8a; font-size: 14px; line-height: 20px;">
                                                    If you didn't request a password reset, please ignore this email and ensure your account is secure. 
                                                    Consider changing your password if you suspect unauthorized access.
                                                </p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                            
                            <!-- Footer -->
                            <tr>
                                <td style="padding: 30px 40px; background-color: #f8f9fa; border-top: 1px solid #e5e5e5; border-radius: 0 0 8px 8px;">
                                    <p style="margin: 0 0 10px; color: #6b7280; font-size: 14px; line-height: 20px; text-align: center;">
                                        Thank you for using our service
                                    </p>
                                    <p style="margin: 0; color: #9ca3af; font-size: 12px; line-height: 18px; text-align: center;">
                                        This is an automated email. Please do not reply.
                                    </p>
                                </td>
                            </tr>
                            
                        </table>
                        
                        <!-- Bottom Info -->
                        <table role="presentation" style="width: 600px; border-collapse: collapse; margin-top: 20px;">
                            <tr>
                                <td style="padding: 0 40px; text-align: center;">
                                    <p style="margin: 0; color: #9ca3af; font-size: 12px; line-height: 18px;">
                                        © 2025 BQMUSIC. All rights reserved.
                                    </p>
                                    <p style="margin: 10px 0 0; color: #9ca3af; font-size: 12px; line-height: 18px;">
                                        For security reasons, this email was sent to verify your identity.
                                    </p>
                                </td>
                            </tr>
                        </table>
                        
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """;

        return template.replace("{{EMAIL}}", email)
                .replace("{{OTP}}", otpCode);
    }
    public String buildOtpEmailTemplate(String otpCode, String email) {
        String template = """
                <!DOCTYPE html>
                        <html lang="en">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Account Verification</title>
                        </head>
                        <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5;">
                            <table role="presentation" style="width: 100%; border-collapse: collapse; background-color: #f5f5f5;">
                                <tr>
                                    <td align="center" style="padding: 40px 0;">
                                        <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                           \s
                                            <!-- Header -->
                                            <tr>
                                                <td style="padding: 40px 40px 30px; text-align: center; border-bottom: 1px solid #e5e5e5;">
                                                    <h1 style="margin: 0; color: #1a1a1a; font-size: 28px; font-weight: 600;">
                                                        Account Verification
                                                    </h1>
                                                </td>
                                            </tr>
                                           \s
                                            <!-- Content -->
                                            <tr>
                                                <td style="padding: 40px;">
                                                    <p style="margin: 0 0 20px; color: #4a4a4a; font-size: 16px; line-height: 24px;">
                                                        Hello,
                                                    </p>
                                                    <p style="margin: 0 0 30px; color: #4a4a4a; font-size: 16px; line-height: 24px;">
                                                        You have requested to register an account using the email <strong>{{EMAIL}}</strong>.
                                                        Please use the OTP code below to complete your registration:
                                                    </p>
                                                   \s
                                                    <!-- OTP Box -->
                                                    <table role="presentation" style="width: 100%; border-collapse: collapse; margin: 30px 0;">
                                                        <tr>
                                                            <td align="center" style="padding: 30px; background-color: #f8f9fa; border-radius: 8px; border: 2px dashed #d1d5db;">
                                                                <div style="font-size: 42px; font-weight: 700; letter-spacing: 8px; color: #2563eb; font-family: 'Courier New', monospace;">
                                                                    {{OTP}}
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                   \s
                                                    <!-- Warning Box -->
                                                    <table role="presentation" style="width: 100%; border-collapse: collapse; margin: 30px 0;">
                                                        <tr>
                                                            <td style="padding: 20px; background-color: #fef3c7; border-left: 4px solid #f59e0b; border-radius: 4px;">
                                                                <p style="margin: 0; color: #92400e; font-size: 14px; line-height: 20px;">
                                                                    ⚠️ <strong>Note:</strong> This OTP code is valid for <strong>90 seconds</strong>.
                                                                    Please do not share this code with anyone.
                                                                </p>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                   \s
                                                    <p style="margin: 30px 0 0; color: #6b7280; font-size: 14px; line-height: 20px;">
                                                        If you did not request account registration, please ignore this email.
                                                    </p>
                                                </td>
                                            </tr>
                                           \s
                                            <!-- Footer -->
                                            <tr>
                                                <td style="padding: 30px 40px; background-color: #f8f9fa; border-top: 1px solid #e5e5e5; border-radius: 0 0 8px 8px;">
                                                    <p style="margin: 0 0 10px; color: #6b7280; font-size: 14px; line-height: 20px; text-align: center;">
                                                        Thank you for using our service
                                                    </p>
                                                    <p style="margin: 0; color: #9ca3af; font-size: 12px; line-height: 18px; text-align: center;">
                                                        This email was sent automatically. Please do not reply.
                                                    </p>
                                                </td>
                                            </tr>
                                           \s
                                        </table>
                                       \s
                                        <!-- Bottom Info -->
                                        <table role="presentation" style="width: 600px; border-collapse: collapse; margin-top: 20px;">
                                            <tr>
                                                <td style="padding: 0 40px; text-align: center;">
                                                    <p style="margin: 0; color: #9ca3af; font-size: 12px; line-height: 18px;">
                                                        © 2026 BQMUSIC. All rights reserved.
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>
                                       \s
                                    </td>
                                </tr>
                            </table>
                        </body>
                        </html>
       
        """;

        return template.replace("{{EMAIL}}", email)
                .replace("{{OTP}}", otpCode);
    }
}
