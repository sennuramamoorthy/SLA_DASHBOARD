package com.sla.dashboard.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jobName;

    @Column(nullable = false)
    private String emailSubjectPattern;

    @Column(nullable = false)
    private LocalTime slaTime;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public Job() {}

    public Job(Long id, String jobName, String emailSubjectPattern, LocalTime slaTime, String description, boolean active) {
        this.id = id;
        this.jobName = jobName;
        this.emailSubjectPattern = emailSubjectPattern;
        this.slaTime = slaTime;
        this.description = description;
        this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }

    public String getEmailSubjectPattern() { return emailSubjectPattern; }
    public void setEmailSubjectPattern(String emailSubjectPattern) { this.emailSubjectPattern = emailSubjectPattern; }

    public LocalTime getSlaTime() { return slaTime; }
    public void setSlaTime(LocalTime slaTime) { this.slaTime = slaTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public static JobBuilder builder() { return new JobBuilder(); }

    public static class JobBuilder {
        private Long id;
        private String jobName;
        private String emailSubjectPattern;
        private LocalTime slaTime;
        private String description;
        private boolean active = true;

        public JobBuilder id(Long id) { this.id = id; return this; }
        public JobBuilder jobName(String jobName) { this.jobName = jobName; return this; }
        public JobBuilder emailSubjectPattern(String emailSubjectPattern) { this.emailSubjectPattern = emailSubjectPattern; return this; }
        public JobBuilder slaTime(LocalTime slaTime) { this.slaTime = slaTime; return this; }
        public JobBuilder description(String description) { this.description = description; return this; }
        public JobBuilder active(boolean active) { this.active = active; return this; }

        public Job build() {
            return new Job(id, jobName, emailSubjectPattern, slaTime, description, active);
        }
    }
}
