package com.intermediate.Blog.Application.ServiceLayer;


import com.intermediate.Blog.Application.Exception.ResourceNotFoundException;
import com.intermediate.Blog.Application.Models.PasswordReset;
import com.intermediate.Blog.Application.Repositories.PasswordResetRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailService {


    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    public void sendMail(String toEmail, String otp, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Verify your email – Go-Connect");

            String htmlContent = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify your email – Go-Connect</title>
</head>
<body style="margin:0; padding:0; background-color:#eef2f6; font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;">
    <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#eef2f6; padding:40px 20px;">
        <tr>
            <td align="center">
                <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                    <tr>
                        <td style="padding:28px 32px 24px; border-bottom:1px solid #e2e8f0;">
                            <p style="margin:0; font-size:22px; font-weight:700;">
                                <span style="color:#1e293b;">Go-</span><span style="color:#2563eb;">Connect</span>
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:28px 32px;">
                            <h1 style="margin:0 0 8px; font-size:20px; font-weight:600; color:#1e293b;">Verify your email</h1>
                            <p style="margin:0 0 20px; font-size:15px; line-height:22px; color:#64748b;">Hi <strong style="color:#1e293b;">{{USERNAME}}</strong>,</p>
                            <p style="margin:0 0 24px; font-size:15px; line-height:22px; color:#475569;">
                                Thanks for signing up. Use the code below to complete your registration and verify your email address.
                            </p>
                            <table cellpadding="0" cellspacing="0" style="margin:0 0 24px;">
                                <tr>
                                    <td style="background:#eff6ff; border:1px solid #bfdbfe; border-radius:8px; padding:14px 24px; font-size:26px; font-weight:700; letter-spacing:6px; color:#1e293b; font-family:ui-monospace, monospace;">
                                        {{OTP_CODE}}
                                    </td>
                                </tr>
                            </table>
                            <p style="margin:0; font-size:14px; line-height:20px; color:#64748b;">This code expires in <strong>10 minutes</strong>. If you didn’t request this, you can ignore this email.</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:16px 32px; background:#f8fafc; border-top:1px solid #e2e8f0; font-size:12px; color:#94a3b8; text-align:center;">
                            © 2025 Go-Connect. All rights reserved.
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
""";

            htmlContent = htmlContent.replace("{{OTP_CODE}}", otp).replace("{{USERNAME}}", username);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email " + e.getMessage());
        }
    }

    public void sendWelcomeMail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Welcome to Go-Connect");

            String loginLink = "http://localhost:5173/login";
            String htmlContent = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Welcome to Go-Connect</title>
</head>
<body style="margin:0; padding:0; background-color:#eef2f6; font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;">
    <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#eef2f6; padding:40px 20px;">
        <tr>
            <td align="center">
                <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                    <tr>
                        <td style="padding:28px 32px 24px; border-bottom:1px solid #e2e8f0;">
                            <p style="margin:0; font-size:22px; font-weight:700;">
                                <span style="color:#1e293b;">Go-</span><span style="color:#2563eb;">Connect</span>
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:28px 32px;">
                            <h1 style="margin:0 0 8px; font-size:20px; font-weight:600; color:#1e293b;">Welcome to Go-Connect</h1>
                            <p style="margin:0 0 20px; font-size:15px; line-height:22px; color:#64748b;">Hi <strong style="color:#1e293b;">{{USERNAME}}</strong>,</p>
                            <p style="margin:0 0 24px; font-size:15px; line-height:22px; color:#475569;">
                                Your email is verified and your account is active. You can sign in and start exploring your feed, posting, and connecting with others.
                            </p>
                            <table cellpadding="0" cellspacing="0" style="margin:0 0 24px;">
                                <tr>
                                    <td>
                                        <a href="{{LOGIN_LINK}}" style="display:inline-block; background:#2563eb; color:#ffffff; padding:12px 24px; text-decoration:none; border-radius:8px; font-size:15px; font-weight:500;">Sign in to Go-Connect</a>
                                    </td>
                                </tr>
                            </table>
                            <p style="margin:0; font-size:14px; line-height:20px; color:#64748b;">If you didn’t create this account, you can ignore this email.</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:16px 32px; background:#f8fafc; border-top:1px solid #e2e8f0; font-size:12px; color:#94a3b8; text-align:center;">
                            © 2025 Go-Connect. All rights reserved.
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
""";

            htmlContent = htmlContent.replace("{{USERNAME}}", username).replace("{{LOGIN_LINK}}", loginLink);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void sendLoginEmail(String toEmail, String otp, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your login code – Go-Connect");

            String htmlContent = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your login code – Go-Connect</title>
</head>
<body style="margin:0; padding:0; background-color:#eef2f6; font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;">
    <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#eef2f6; padding:40px 20px;">
        <tr>
            <td align="center">
                <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                    <tr>
                        <td style="padding:28px 32px 24px; border-bottom:1px solid #e2e8f0;">
                            <p style="margin:0; font-size:22px; font-weight:700;">
                                <span style="color:#1e293b;">Go-</span><span style="color:#2563eb;">Connect</span>
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:28px 32px;">
                            <h1 style="margin:0 0 8px; font-size:20px; font-weight:600; color:#1e293b;">Sign-in verification</h1>
                            <p style="margin:0 0 20px; font-size:15px; line-height:22px; color:#64748b;">Hi <strong style="color:#1e293b;">{{USERNAME}}</strong>,</p>
                            <p style="margin:0 0 24px; font-size:15px; line-height:22px; color:#475569;">
                                We received a sign-in attempt for your account. Use the code below to complete login.
                            </p>
                            <table cellpadding="0" cellspacing="0" style="margin:0 0 24px;">
                                <tr>
                                    <td style="background:#eff6ff; border:1px solid #bfdbfe; border-radius:8px; padding:14px 24px; font-size:26px; font-weight:700; letter-spacing:6px; color:#1e293b; font-family:ui-monospace, monospace;">
                                        {{OTP_CODE}}
                                    </td>
                                </tr>
                            </table>
                            <p style="margin:0; font-size:14px; line-height:20px; color:#64748b;">This code expires in <strong>3 minutes</strong>. Don’t share it with anyone.</p>
                            <p style="margin:16px 0 0; font-size:14px; line-height:20px; color:#64748b;">If this wasn’t you, we recommend changing your password after signing in.</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:16px 32px; background:#f8fafc; border-top:1px solid #e2e8f0; font-size:12px; color:#94a3b8; text-align:center;">
                            © 2025 Go-Connect. All rights reserved.
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
""";

            htmlContent = htmlContent.replace("{{OTP_CODE}}", otp).replace("{{USERNAME}}", username);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public void sendResetMail(String email , String token) throws MessagingException {
        String link = "http://localhost:5173/reset-password?token=" + token;



        String htmlContent = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset your password – Go-Connect</title>
</head>
<body style="margin:0; padding:0; background-color:#eef2f6; font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;">
    <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#eef2f6; padding:40px 20px;">
        <tr>
            <td align="center">
                <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                    <tr>
                        <td style="padding:28px 32px 24px; border-bottom:1px solid #e2e8f0;">
                            <p style="margin:0; font-size:22px; font-weight:700;">
                                <span style="color:#1e293b;">Go-</span><span style="color:#2563eb;">Connect</span>
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:28px 32px;">
                            <h1 style="margin:0 0 8px; font-size:20px; font-weight:600; color:#1e293b;">Reset your password</h1>
                            <p style="margin:0 0 20px; font-size:15px; line-height:22px; color:#64748b;">Hi <strong style="color:#1e293b;">${USERNAME}</strong>,</p>
                            <p style="margin:0 0 24px; font-size:15px; line-height:22px; color:#475569;">
                                We received a request to reset the password for your Go-Connect account. Click the button below to set a new password.
                            </p>
                            <table cellpadding="0" cellspacing="0" style="margin:0 0 24px;">
                                <tr>
                                    <td>
                                        <a href="${RESET_LINK}" style="display:inline-block; background:#2563eb; color:#ffffff; padding:12px 24px; text-decoration:none; border-radius:8px; font-size:15px; font-weight:500;">Reset password</a>
                                    </td>
                                </tr>
                            </table>
                            <p style="margin:0; font-size:14px; line-height:20px; color:#64748b;">This link expires in <strong>15 minutes</strong>. If you didn’t request this, you can ignore this email and your password will stay the same.</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:16px 32px; background:#f8fafc; border-top:1px solid #e2e8f0; font-size:12px; color:#94a3b8; text-align:center;">
                            © 2025 Go-Connect. All rights reserved.
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
""";

        PasswordReset passwordReset = passwordResetRepository.findByToken(token).orElseThrow(()-> new ResourceNotFoundException("PasswordReset" , "token", token));



        htmlContent = htmlContent.replace("${RESET_LINK}", link);
        htmlContent = htmlContent.replace("${USERNAME}" , passwordReset.getUser().getUsername());

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("Password Reset Request");
        helper.setText(htmlContent , true);
        mailSender.send(message);
    }
}
