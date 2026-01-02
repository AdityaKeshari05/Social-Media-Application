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

    public void sendMail(String toEmail , String otp){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your One Time Password (OTP) for Verification");

            String htmlContent = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>OTP Verification</title>
</head>
<body style="font-family: Arial, sans-serif; background-color:#f4f4f7; padding: 40px;">
    <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <td align="center">

                <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff; border-radius:10px; padding:30px;">
                    
                    <tr>
                        <td align="center" style="padding-bottom:20px;">
                            <h2 style="color:#333333; margin:0; font-size:24px;">Email Verification</h2>
                        </td>
                    </tr>

                    <tr>
                        <td style="color:#555555; font-size:16px; line-height:24px;">
                            <p>Hello,</p>
                            <p>
                                Thank you for registering!  
                                To complete your signup and verify your email address, please use the One-Time Password (OTP) provided below:
                            </p>
                        </td>
                    </tr>

                    <tr>
                        <td align="center" style="padding: 25px 0;">
                            <div style="background:#4b7bec; color:white; display:inline-block; padding:15px 25px; border-radius:8px; font-size:28px; letter-spacing:4px; font-weight:bold;">
                                {{OTP_CODE}}
                            </div>
                        </td>
                    </tr>

                    <tr>
                        <td style="color:#555555; font-size:16px; line-height:24px;">
                            <p>This OTP is valid for <strong>10 minutes</strong>.</p>
                            <p>If you did not request this, you can safely ignore this email.</p>
                        </td>
                    </tr>

                    <tr>
                        <td style="padding-top:30px; color:#999999; font-size:14px; text-align:center;">
                            © 2025 YourApp. All Rights Reserved.
                        </td>
                    </tr>

                </table>

            </td>
        </tr>
    </table>
</body>
</html>
""";

                htmlContent = htmlContent.replace("{{OTP_CODE}}",otp);
                helper.setText(htmlContent, true);
                mailSender.send(message);


        }catch (Exception e){
            throw new RuntimeException("Failed to send email "+ e.getMessage());
        }

    }

    public void sendWelcomeMail(String toEmail , String username){

        try{

            MimeMessage message  = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message , true, "UTF-8");

            String htmlContent =
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "    <meta charset='UTF-8'>" +
                            "    <title>Welcome to Our Platform</title>" +
                            "</head>" +
                            "<body style='font-family: Arial, sans-serif; background-color:#f4f4f7; padding: 40px;'>" +
                            "    <table width='100%' cellpadding='0' cellspacing='0'>" +
                            "        <tr>" +
                            "            <td align='center'>" +

                            "                <table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff; border-radius:10px; padding:30px;'>" +

                            "                    <tr>" +
                            "                        <td align='center' style='padding-bottom:20px;'>" +
                            "                            <h2 style='color:#333333; margin:0; font-size:26px;'>Welcome to Our Community!</h2>" +
                            "                        </td>" +
                            "                    </tr>" +

                            "                    <tr>" +
                            "                        <td style='color:#555555; font-size:16px; line-height:24px;'>" +
                            "                            <p>Hi <strong>{{USERNAME}}</strong>,</p>" +
                            "                            <p>Great news! Your email has been successfully verified and your account is now active.</p>" +
                            "                            <p>We're excited to have you on board. You can now log in and start using all the features of our platform.</p>" +
                            "                        </td>" +
                            "                    </tr>" +

                            "                    <tr>" +
                            "                        <td align='center' style='padding: 25px 0;'>" +
                            "                            <a href='{{LOGIN_LINK}}' style='background:#4b7bec; color:white; padding:12px 25px; text-decoration:none; border-radius:6px; font-size:16px;'>Login to Your Account</a>" +
                            "                        </td>" +
                            "                    </tr>" +

                            "                    <tr>" +
                            "                        <td style='color:#555555; font-size:16px; line-height:24px;'>" +
                            "                            <p>If you did not register this account, please ignore this email.</p>" +
                            "                        </td>" +
                            "                    </tr>" +

                            "                    <tr>" +
                            "                        <td style='padding-top:30px; color:#999999; font-size:14px; text-align:center;'>" +
                            "                            © 2025 YourApp. All Rights Reserved." +
                            "                        </td>" +
                            "                    </tr>" +

                            "                </table>" +

                            "            </td>" +
                            "        </tr>" +
                            "    </table>" +
                            "</body>" +
                            "</html>";


            helper.setTo(toEmail);
            htmlContent = htmlContent.replace("{{USERNAME}}" , username);
            htmlContent = htmlContent.replace("{{LOGIN_LINK}}" , "https://localhost:8080/api/posts");
            helper.setText(htmlContent , true);
            helper.setSubject("Welcome Email");
            mailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

    }

    public void sendLoginEmail(String toEmail , String otp){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String htmlContent =  """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Login Verification</title>
</head>
<body style="margin:0; padding:0; background-color:#f4f4f4; font-family:Arial, sans-serif;">
    <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f4; padding:30px 0;">
        <tr>
            <td align="center">

                <table width="600" style="background:#ffffff; border-radius:10px; padding:30px;">
                    <tr>
                        <td align="center" style="padding-bottom:20px;">
                            <h2 style="color:#333; margin:0;">Login Verification Required</h2>
                        </td>
                    </tr>

                    <tr>
                        <td style="color:#555; font-size:15px; line-height:24px;">
                            <p>Hello,</p>

                            <p>We received a login attempt to your account. To continue, please use the One-Time Password (OTP) below to verify it is you.</p>

                            <p style="font-size:18px; margin:20px 0 10px 0;">Your Login OTP:</p>

                            <p style="font-size:32px; font-weight:bold; color:black; letter-spacing:4px; margin:0; padding:10px 0;">
                                ${OTP_CODE}
                            </p>

                            <p>This OTP is valid for <b>3 minutes</b>. Please do not share it with anyone.</p>

                            <p>If this was not you, we recommend updating your password immediately.</p>

                            <br />

                            <p>Regards,<br /><b>Your App Team</b></p>
                        </td>
                    </tr>

                    <tr>
                        <td align="center" style="padding-top:30px; color:#999; font-size:12px;">
                            © 2025 Your Application. All Rights Reserved.
                        </td>
                    </tr>
                </table>

            </td>
        </tr>
    </table>
</body>
</html>
""";
            htmlContent = htmlContent.replace("${OTP_CODE}", otp);
            helper.setTo(toEmail);
            helper.setText(htmlContent, true);
            helper.setSubject("Your Single-Use Code");
            mailSender.send(message);
        }catch(Exception e){
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
    <title>Reset Your Password - Go-Connect</title>
</head>
<body style="margin:0; padding:0; background-color:#f4f6f8; font-family:Arial, sans-serif;">

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td align="center" style="padding:40px 0;">
            <table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 4px 12px rgba(0,0,0,0.1);">

                <!-- HEADER -->
                <tr>
                    <td style="background:#0d6efd; padding:20px; text-align:center;">
                        <h1 style="color:#ffffff; margin:0;">Go-Connect</h1>
                    </td>
                </tr>

                <!-- BODY -->
                <tr>
                    <td style="padding:30px; color:#333;">
                        <h2 style="margin-top:0;">Reset Your Password</h2>

                        <p>Hello, ${USERNAME},</p>

                        <p>We received a request to reset your password for your <strong>Go-Connect</strong> account.</p>

                        <p>Click the button below to reset your password:</p>

                        <!-- BUTTON -->
                        <div style="text-align:center; margin:30px 0;">
                            <a href="${RESET_LINK}"3
                               style="background:#0d6efd; color:#ffffff; padding:14px 28px; text-decoration:none; border-radius:6px; display:inline-block; font-size:16px;">
                               Reset Password
                            </a>
                        </div>

                        <p>This link will expire in <strong>15 minutes</strong> for security reasons.</p>

                        <p>If you did not request this password reset, please ignore this email. Your password will remain unchanged.</p>

                        <p style="margin-top:40px;">
                            Regards,<br>
                            <strong>Go-Connect Team</strong>
                        </p>
                    </td>
                </tr>

                <!-- FOOTER -->
                <tr>
                    <td style="background:#f1f1f1; padding:15px; text-align:center; font-size:12px; color:#777;">
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
