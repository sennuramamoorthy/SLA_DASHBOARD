package com.sla.dashboard.scheduler;

import com.sla.dashboard.service.EmailMonitorService;
import com.sla.dashboard.service.JobExecutionService;
import com.sla.dashboard.service.SummaryEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SlaMonitorScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaMonitorScheduler.class);

    private final EmailMonitorService emailMonitorService;
    private final JobExecutionService executionService;
    private final SummaryEmailService summaryEmailService;

    @Value("${scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public SlaMonitorScheduler(EmailMonitorService emailMonitorService,
                               JobExecutionService executionService,
                               SummaryEmailService summaryEmailService) {
        this.emailMonitorService = emailMonitorService;
        this.executionService = executionService;
        this.summaryEmailService = summaryEmailService;
    }

    @Scheduled(cron = "${scheduler.daily-init.cron:0 0 0 * * ?}")
    public void initializeDailyExecutions() {
        if (!schedulerEnabled) return;
        log.info("Initializing daily job executions");
        executionService.initializeDailyExecutions(LocalDate.now());
    }

    @Scheduled(fixedRateString = "${scheduler.email-check.interval-ms:300000}")
    public void checkEmails() {
        if (!schedulerEnabled) return;
        log.info("Running scheduled email check");
        emailMonitorService.checkEmails();
    }

    @Scheduled(cron = "${scheduler.mark-missed.cron:0 0 * * * ?}")
    public void markMissedJobs() {
        if (!schedulerEnabled) return;
        log.info("Checking for missed SLA jobs");
        executionService.markMissedJobs(LocalDate.now());
    }

    @Scheduled(cron = "${scheduler.daily-summary.cron:0 0 18 * * ?}")
    public void sendDailySummary() {
        if (!schedulerEnabled) return;
        log.info("Sending daily SLA summary");
        summaryEmailService.sendDailySummary(LocalDate.now());
    }
}
