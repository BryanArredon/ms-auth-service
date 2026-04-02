package com.security.auth_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject("Tu código de verificación - MS Enfermería");
            
            String htmlMsg = "<div style='font-family: Arial, sans-serif; text-align: center; color: #333;'>"
                    + "<h2>Verificación de Inicio de Sesión</h2>"
                    + "<p>Has solicitado iniciar sesión. Por favor, usa el siguiente código de 6 dígitos para completar el proceso:</p>"
                    + "<h1 style='color: #4CAF50; font-size: 36px; padding: 10px; border: 2px dashed #4CAF50; display: inline-block;'>" + otp + "</h1>"
                    + "<p>Este código <b>expirará en 5 minutos</b> y es de un solo uso.</p>"
                    + "<p>Si no fuiste tú, por favor contacta al administrador.</p>"
                    + "</div>";
            
            helper.setText(htmlMsg, true);
            javaMailSender.send(message);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo de MFA: " + e.getMessage());
        }
    }
}
