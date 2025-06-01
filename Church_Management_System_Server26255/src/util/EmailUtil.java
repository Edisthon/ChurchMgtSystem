package util;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {

    // IMPORTANT: Configure these properties for your email server
    // Ideally, load from a configuration file instead of hardcoding.
    // THESE ARE PLACEHOLDERS AND WILL NOT WORK WITHOUT YOUR ACTUAL CREDENTIALS.
    private static final String SMTP_HOST = "smtp.gmail.com"; // e.g., "smtp.gmail.com"
    private static final String SMTP_PORT = "587"; // e.g., "587" for TLS, "465" for SSL
    private static final String SMTP_USER = "baremag34@gmail.com"; // Your email address used for sending
    private static final String SMTP_PASSWORD = "wawuexreumdyutzr"; // Your email password or app-specific password
    private static final boolean SMTP_AUTH = true; // Usually true
    private static final boolean SMTP_STARTTLS_ENABLE = true; // true for TLS (recommended), false for SSL on port 465

    public static boolean sendOtpEmail(String recipientEmail, String otpCode) {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", String.valueOf(SMTP_AUTH));

        if (SMTP_STARTTLS_ENABLE) {
            props.put("mail.smtp.starttls.enable", "true");
        } else {
            // For SSL (e.g., port 465), you might need properties like:
            // props.put("mail.smtp.socketFactory.port", SMTP_PORT);
            // props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            // props.put("mail.smtp.ssl.enable", "true"); // For Jakarta Mail
        }
        // Depending on your provider, you might need to trust the host or add other specific props
        // props.put("mail.smtp.ssl.trust", SMTP_HOST); // Example for trusting specific hosts

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
            }
        });

        // session.setDebug(true); // Enable for debugging mail session issues

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER)); // Can be same as SMTP_USER or a no-reply alias
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your One-Time Password (OTP) for Church Management System");

            String emailContent = "Dear User,\n\n" +
                                  "Your One-Time Password (OTP) for the Church Management System is: " + otpCode + "\n\n" +
                                  "This OTP is valid for a short period (typically 5 minutes).\n\n" +
                                  "If you did not request this OTP, please ignore this email or contact support if you have concerns.\n\n" +
                                  "Thank you,\n" +
                                  "Church Management System";
            message.setText(emailContent);

            Transport.send(message);
            System.out.println("OTP email sent successfully to " + recipientEmail);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Error sending OTP email to " + recipientEmail + ": " + e.getMessage());
            return false;
        }
    }
}
