package com.sla.dashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_executions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"job_id", "execution_date"})
})
public class JobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "execution_date", nullable = false)
    private LocalDate executionDate;

    @Column
    private LocalDateTime completionTime;

    @Column
    private String emailSubject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    @Column
    private boolean withinSla;

    @Column
    private Long slaDifferenceMinutes;

    public enum ExecutionStatus {
        PENDING,
        COMPLETED,
        MISSED
    }

    public JobExecution() {}

    public JobExecution(Long id, Job job, LocalDate executionDate, LocalDateTime completionTime,
                        String emailSubject, ExecutionStatus status, boolean withinSla, Long slaDifferenceMinutes) {
        this.id = id;
        this.job = job;
        this.executionDate = executionDate;
        this.completionTime = completionTime;
        this.emailSubject = emailSubject;
        this.status = status;
        this.withinSla = withinSla;
        this.slaDifferenceMinutes = slaDifferenceMinutes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }

    public LocalDate getExecutionDate() { return executionDate; }
    public void setExecutionDate(LocalDate executionDate) { this.executionDate = executionDate; }

    public LocalDateTime getCompletionTime() { return completionTime; }
    public void setCompletionTime(LocalDateTime completionTime) { this.completionTime = completionTime; }

    public String getEmailSubject() { return emailSubject; }
    public void setEmailSubject(String emailSubject) { this.emailSubject = emailSubject; }

    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }

    public boolean isWithinSla() { return withinSla; }
    public void setWithinSla(boolean withinSla) { this.withinSla = withinSla; }

    public Long getSlaDifferenceMinutes() { return slaDifferenceMinutes; }
    public void setSlaDifferenceMinutes(Long slaDifferenceMinutes) { this.slaDifferenceMinutes = slaDifferenceMinutes; }

    public static JobExecutionBuilder builder() { return new JobExecutionBuilder(); }

    public static class JobExecutionBuilder {
        private Long id;
        private Job job;
        private LocalDate executionDate;
        private LocalDateTime completionTime;
        private String emailSubject;
        private ExecutionStatus status;
        private boolean withinSla;
        private Long slaDifferenceMinutes;

        public JobExecutionBuilder id(Long id) { this.id = id; return this; }
        public JobExecutionBuilder job(Job job) { this.job = job; return this; }
        public JobExecutionBuilder executionDate(LocalDate executionDate) { this.executionDate = executionDate; return this; }
        public JobExecutionBuilder completionTime(LocalDateTime completionTime) { this.completionTime = completionTime; return this; }
        public JobExecutionBuilder emailSubject(String emailSubject) { this.emailSubject = emailSubject; return this; }
        public JobExecutionBuilder status(ExecutionStatus status) { this.status = status; return this; }
        public JobExecutionBuilder withinSla(boolean withinSla) { this.withinSla = withinSla; return this; }
        public JobExecutionBuilder slaDifferenceMinutes(Long slaDifferenceMinutes) { this.slaDifferenceMinutes = slaDifferenceMinutes; return this; }

        public JobExecution build() {
            return new JobExecution(id, job, executionDate, completionTime, emailSubject, status, withinSla, slaDifferenceMinutes);
        }
    }
}
