package com.sla.dashboard.service;

import com.sla.dashboard.entity.Job;
import com.sla.dashboard.entity.JobExecution;
import com.sla.dashboard.entity.JobExecution.ExecutionStatus;
import com.sla.dashboard.repository.JobExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class JobExecutionService {

    private static final Logger log = LoggerFactory.getLogger(JobExecutionService.class);

    private final JobExecutionRepository executionRepository;
    private final JobService jobService;

    public JobExecutionService(JobExecutionRepository executionRepository, JobService jobService) {
        this.executionRepository = executionRepository;
        this.jobService = jobService;
    }

    public List<JobExecution> getExecutionsForDate(LocalDate date) {
        return executionRepository.findByExecutionDateOrderByJobJobName(date);
    }

    public Optional<JobExecution> getExecutionForJobAndDate(Job job, LocalDate date) {
        return executionRepository.findByJobAndExecutionDate(job, date);
    }

    @Transactional
    public void initializeDailyExecutions(LocalDate date) {
        List<Job> activeJobs = jobService.getAllActiveJobs();
        for (Job job : activeJobs) {
            if (!executionRepository.existsByJobAndExecutionDate(job, date)) {
                JobExecution execution = JobExecution.builder()
                        .job(job)
                        .executionDate(date)
                        .status(ExecutionStatus.PENDING)
                        .withinSla(false)
                        .build();
                executionRepository.save(execution);
                log.info("Initialized execution for job {} on {}", job.getJobName(), date);
            }
        }
    }

    @Transactional
    public JobExecution recordCompletion(Job job, LocalDate date, LocalDateTime completionTime, String emailSubject) {
        JobExecution execution = executionRepository.findByJobAndExecutionDate(job, date)
                .orElseGet(() -> JobExecution.builder()
                        .job(job)
                        .executionDate(date)
                        .build());

        execution.setCompletionTime(completionTime);
        execution.setEmailSubject(emailSubject);
        execution.setStatus(ExecutionStatus.COMPLETED);

        LocalTime completionLocalTime = completionTime.toLocalTime();
        LocalTime slaTime = job.getSlaTime();
        boolean withinSla = !completionLocalTime.isAfter(slaTime);
        execution.setWithinSla(withinSla);

        long diffMinutes = ChronoUnit.MINUTES.between(slaTime, completionLocalTime);
        execution.setSlaDifferenceMinutes(diffMinutes);

        log.info("Recorded completion for job {} at {} - Within SLA: {} (diff: {} minutes)",
                job.getJobName(), completionTime, withinSla, diffMinutes);

        return executionRepository.save(execution);
    }

    @Transactional
    public void markMissedJobs(LocalDate date) {
        List<JobExecution> executions = executionRepository.findByExecutionDate(date);
        LocalTime now = LocalTime.now();

        for (JobExecution execution : executions) {
            if (execution.getStatus() == ExecutionStatus.PENDING) {
                LocalTime slaTime = execution.getJob().getSlaTime();
                if (now.isAfter(slaTime)) {
                    execution.setStatus(ExecutionStatus.MISSED);
                    execution.setWithinSla(false);
                    long diffMinutes = ChronoUnit.MINUTES.between(slaTime, now);
                    execution.setSlaDifferenceMinutes(diffMinutes);
                    executionRepository.save(execution);
                    log.warn("Marked job {} as MISSED for date {}", execution.getJob().getJobName(), date);
                }
            }
        }
    }

    public SlaStats calculateDailyStats(LocalDate date) {
        List<JobExecution> executions = getExecutionsForDate(date);
        int total = executions.size();
        int completed = 0;
        int withinSla = 0;
        int missed = 0;
        int pending = 0;

        for (JobExecution exec : executions) {
            switch (exec.getStatus()) {
                case COMPLETED -> {
                    completed++;
                    if (exec.isWithinSla()) {
                        withinSla++;
                    }
                }
                case MISSED -> missed++;
                case PENDING -> pending++;
            }
        }

        return new SlaStats(total, completed, withinSla, missed, pending);
    }

    public record SlaStats(int total, int completed, int withinSla, int missed, int pending) {
        public double getSlaCompliancePercent() {
            if (completed == 0) return 0.0;
            return (withinSla * 100.0) / total;
        }
    }
}
