package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine thymeleafTemplateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.frontend.base-url}") // Add a base URL for links
    private String applicationBaseUrl;

    @Value("${app.email.logo.path}") // Default logo path
    private String logoImagePath;

    @Value("${app.email.logo.cid}") // CID for inline image
    private String logoImageCid;

    @Value("${app.frontend.login-path}")
    private String loginPath;

    @Value("${otp.expiry.minutes}")
    private int otpExpiryMinutes;

    @Async // Gửi email bất đồng bộ
    public void sendNewStaffCredentialsEmail(User staff, String rawPassword) {
        log.info("Chuẩn bị gửi email thông tin tài khoản cho nhân viên mới: {}", staff.getEmail());
        String subject = "Thông tin tài khoản Hệ thống Quản lý Sức khỏe Học đường";

        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("fullName", staff.getFullName());
        thymeleafContext.setVariable("email", staff.getEmail());
        thymeleafContext.setVariable("password", rawPassword);
        thymeleafContext.setVariable("loginUrl", applicationBaseUrl + loginPath); // Hoặc trang đăng nhập cụ thể
        thymeleafContext.setVariable("logoImage", logoImageCid); // CID for inline image
        thymeleafContext.setVariable("otpExpiryMinutes", otpExpiryMinutes);

        String htmlBody = thymeleafTemplateEngine.process("emails/new-staff-credentials-email", thymeleafContext);

        try {
            sendHtmlEmail(staff.getEmail(), subject, htmlBody); // Gọi method có throws
            log.info("Đã gửi email thông tin tài khoản cho: {}", staff.getEmail());
        } catch (MessagingException e) {
            // Log đã được thực hiện trong sendHtmlEmail
            // Nếu muốn, có thể log thêm context ở đây
            log.error("Không thể gửi email thông tin tài khoản cho {} do lỗi: {}", staff.getEmail(), e.getMessage());
        }
    }

    @Async
    public void sendOtpEmail(String email, String otp) {
        log.info("Chuẩn bị gửi email OTP cho: {}", email);
        String subject = "Mã OTP đặt lại mật khẩu Hệ thống Quản lý Sức khỏe Học đường";

        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("otp", otp);
        thymeleafContext.setVariable("logoImage", logoImageCid);
        thymeleafContext.setVariable("otpExpiryMinutes", otpExpiryMinutes);

        String htmlBody = thymeleafTemplateEngine.process("emails/otp-email", thymeleafContext);
        try {
            sendHtmlEmail(email, subject, htmlBody); // Gọi method có throws
            log.info("Đã gửi email OTP cho: {}", email);
        } catch (MessagingException e) {
            log.error("Không thể gửi email OTP cho {} do lỗi: {}", email, e.getMessage());
        }
    }


    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true for multipart
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true for HTML

            ClassPathResource logoResource = new ClassPathResource(logoImagePath);
            if (logoResource.exists()) {
                helper.addInline(logoImageCid, logoResource, "image/png");
            } else {
                log.warn("Không tìm thấy file logo tại: static/images/logo.png");
            }

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email HTML tới {}: {}", to, e.getMessage());
            throw e;
        }
    }
}