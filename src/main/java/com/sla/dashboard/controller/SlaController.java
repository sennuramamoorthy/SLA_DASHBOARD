package com.sla.dashboard.controller;

import com.sla.dashboard.entity.Job;
import com.sla.dashboard.entity.JobExecution;
import com.sla.dashboard.service.EmailMonitorService;
import com.sla.dashboard.service.JobExecutionService;
import com.sla.dashboard.service.JobService;
import com.sla.dashboard.service.SummaryEmailService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SlaController {

    private final JobService jobService;
    private final JobExecutionService executionService;
    private final EmailMonitorService emailMonitorService;
    private final SummaryEmailService summaryEmailService;

    public SlaController(JobService jobService, JobExecutionService executionService,
                         EmailMonitorService emailMonitorService, SummaryEmailService summaryEmailService) {
        this.jobService = jobService;
        this.executionService = executionService;
        this.emailMonitorService = emailMonitorService;
        this.summaryEmailService = summaryEmailService;
    }

    @GetMapping("/jobs")
    public List<Job> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/jobs/active")
    public List<Job> getActiveJobs() {
        return jobService.getAllActiveJobs();
    }

    @PostMapping("/jobs")
    public Job createJob(@RequestBody Job job) {
        return jobService.saveJob(job);
    }

    @GetMapping("/executions")
    public List<JobExecution> getExecutions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return executionService.getExecutionsForDate(queryDate);
    }

    @GetMapping("/stats")
    public JobExecutionService.SlaStats getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return executionService.calculateDailyStats(queryDate);
    }

    @PostMapping("/actions/check-emails")
    public ResponseEntity<Map<String, String>> triggerEmailCheck() {
        emailMonitorService.checkEmails();
        return ResponseEntity.ok(Map.of("message", "Email check completed"));
    }

    @PostMapping("/actions/send-summary")
    public ResponseEntity<Map<String, String>> triggerSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate reportDate = date != null ? date : LocalDate.now();
        summaryEmailService.sendDailySummary(reportDate);
        return ResponseEntity.ok(Map.of("message", "Summary email sent for " + reportDate));
    }

    @PostMapping("/actions/initialize")
    public ResponseEntity<Map<String, String>> initializeDay(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate initDate = date != null ? date : LocalDate.now();
        executionService.initializeDailyExecutions(initDate);
        return ResponseEntity.ok(Map.of("message", "Initialized executions for " + initDate));
    }

    @PostMapping("/actions/mark-missed")
    public ResponseEntity<Map<String, String>> markMissed(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate checkDate = date != null ? date : LocalDate.now();
        executionService.markMissedJobs(checkDate);
        return ResponseEntity.ok(Map.of("message", "Marked missed jobs for " + checkDate));
    }
}
