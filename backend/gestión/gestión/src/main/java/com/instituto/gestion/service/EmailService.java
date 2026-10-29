package com.instituto.gestion.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Envío de correos transaccionales del instituto.
 *
 * <p>El envío es asíncrono y nunca propaga errores al flujo que originó la
 * notificación. Si las credenciales SMTP todavía no están configuradas, se
 * registra una advertencia y se omite el envío.</p>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String password;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String remitente,
            @Value("${spring.mail.password:}") String password
    ) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.password = password;
    }

    /**
     * Notifica a los alumnos inscriptos que se agendó un examen.
     *
     * <p>Se ejecuta en segundo plano para que la respuesta HTTP de creación
     * del evento no dependa de la latencia del servidor SMTP.</p>
     */
    @Async
    public void notificarNuevoExamen(
            List<String> destinatarios,
            String nombreCurso,
            String tituloExamen,
            LocalDate fechaExamen,
            LocalTime hora,
            String descripcion
    ) {
        if (destinatarios == null || destinatarios.isEmpty()) {
            log.info("No hay alumnos activos con correo para notificar el examen '{}'.", tituloExamen);
            return;
        }

        if (remitente == null || remitente.isBlank()
                || password == null || password.isBlank()) {
            log.warn("No se enviaron las notificaciones del examen '{}': "
                            + "configurá MAIL_USERNAME y MAIL_PASSWORD para habilitar el envío SMTP.",
                    tituloExamen);
            return;
        }

        try {
            String html = construirHtml(nombreCurso, tituloExamen, fechaExamen, hora, descripcion);
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(remitente);
            helper.setBcc(destinatarios.toArray(String[]::new));
            helper.setSubject("Nuevo examen agendado: " + textoPlano(tituloExamen, "Examen"));
            helper.setText(html, true);

            mailSender.send(mensaje);
            log.info("Notificación del examen '{}' enviada a {} alumno(s).",
                    tituloExamen, destinatarios.size());
        } catch (Exception error) {
            log.error("No se pudo enviar la notificación del examen '{}': {}",
                    tituloExamen, error.getMessage(), error);
        }
    }

    private String construirHtml(
            String nombreCurso,
            String tituloExamen,
            LocalDate fechaExamen,
            LocalTime hora,
            String descripcion
    ) {
        String curso = escapeHtml(textoPlano(nombreCurso, "Curso"));
        String examen = escapeHtml(textoPlano(tituloExamen, "Examen"));
        String fecha = fechaExamen != null ? FORMATO_FECHA.format(fechaExamen) : "A confirmar";
        String horaTexto = hora != null ? FORMATO_HORA.format(hora) + " hs" : "A confirmar";
        String observaciones = descripcion == null || descripcion.isBlank()
                ? "Sin observaciones."
                : escapeHtml(descripcion.trim()).replace("\r\n", "\n").replace("\n", "<br>");

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background:#eef1f5;font-family:'Segoe UI',Arial,sans-serif;color:#1f2933;">
                    <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#eef1f5;padding:24px 12px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:560px;background:#ffffff;border:1px solid #d9e0e8;border-radius:8px;overflow:hidden;">
                                    <tr>
                                        <td style="background:#1f3350;padding:20px 24px;color:#ffffff;">
                                            <div style="font-size:11px;letter-spacing:.8px;text-transform:uppercase;opacity:.8;">Instituto · Gestión Académica</div>
                                            <div style="font-size:21px;font-weight:700;margin-top:5px;">Nuevo examen agendado</div>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:24px;">
                                            <p style="margin:0 0 14px;font-size:14px;">Hola:</p>
                                            <p style="margin:0 0 18px;font-size:14px;line-height:1.6;">
                                                Se agendó un nuevo examen para el curso <strong>{{CURSO}}</strong>.
                                            </p>
                                            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;font-size:14px;">
                                                <tr>
                                                    <td style="padding:10px 12px;background:#f7f9fb;border:1px solid #e2e8f0;color:#64748b;width:34%;">Examen</td>
                                                    <td style="padding:10px 12px;border:1px solid #e2e8f0;font-weight:600;">{{EXAMEN}}</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding:10px 12px;background:#f7f9fb;border:1px solid #e2e8f0;color:#64748b;">Fecha</td>
                                                    <td style="padding:10px 12px;border:1px solid #e2e8f0;">{{FECHA}}</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding:10px 12px;background:#f7f9fb;border:1px solid #e2e8f0;color:#64748b;">Hora</td>
                                                    <td style="padding:10px 12px;border:1px solid #e2e8f0;">{{HORA}}</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding:10px 12px;background:#f7f9fb;border:1px solid #e2e8f0;color:#64748b;vertical-align:top;">Observaciones</td>
                                                    <td style="padding:10px 12px;border:1px solid #e2e8f0;line-height:1.5;">{{OBSERVACIONES}}</td>
                                                </tr>
                                            </table>
                                            <p style="margin:18px 0 0;font-size:13px;color:#64748b;line-height:1.5;">
                                                Si tenés dudas, comunicate con la secretaría del instituto.
                                            </p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:14px 24px;background:#f7f9fb;border-top:1px solid #e2e8f0;color:#64748b;font-size:11px;">
                                            Este mensaje se envió automáticamente porque estás inscripto en el curso.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;

        return html
                .replace("{{CURSO}}", curso)
                .replace("{{EXAMEN}}", examen)
                .replace("{{FECHA}}", fecha)
                .replace("{{HORA}}", horaTexto)
                .replace("{{OBSERVACIONES}}", observaciones);
    }

    private String textoPlano(String valor, String respaldo) {
        return valor == null || valor.isBlank() ? respaldo : valor.trim();
    }

    private String escapeHtml(String valor) {
        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
