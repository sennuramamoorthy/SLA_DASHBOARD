package com.sla.dashboard.service;

import com.sla.dashboard.entity.JobExecution;
import com.sla.dashboard.entity.JobExecution.ExecutionStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SummaryEmailService {

    private static final Logger log = LoggerFactory.getLogger(SummaryEmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final JobExecutionService executionService;

    @Value("${email.summary.recipient}")
    private String recipient;

    @Value("${email.summary.from}")
    private String fromAddress;

    public SummaryEmailService(JavaMailSender mailSender, TemplateEngine templateEngine,
                               JobExecutionService executionService) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.executionService = executionService;
    }

    public void sendDailySummary(LocalDate date) {
        log.info("Sending daily SLA summary for {}", date);

        List<JobExecution> executions = executionService.getExecutionsForDate(date);
        JobExecutionService.SlaStats stats = executionService.calculateDailyStats(date);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(recipient);
            helper.setSubject(buildSubject(date, stats));

            String htmlContent = buildHtmlContent(date, executions, stats);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Daily summary email sent successfully for {}", date);

        } catch (MessagingException e) {
            log.error("Failed to send daily summary email: {}", e.getMessage(), e);
        }
    }

    private String buildSubject(LocalDate date, JobExecutionService.SlaStats stats) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String status = stats.getSlaCompliancePercent() == 100.0 ? "ALL GREEN" :
                stats.missed() > 0 ? "ATTENTION REQUIRED" : "PARTIAL";
        return String.format("SLA Dashboard Report - %s - %s (%.0f%% Compliance)",
                dateStr, status, stats.getSlaCompliancePercent());
    }

    private String buildHtmlContent(LocalDate date, List<JobExecution> executions,
                                    JobExecutionService.SlaStats stats) {
        Context context = new Context();
        context.setVariable("date", date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        context.setVariable("executions", executions);
        context.setVariable("stats", stats);
        context.setVariable("ExecutionStatus", ExecutionStatus.class);
        return templateEngine.process("summary-email", context);
    }
}
