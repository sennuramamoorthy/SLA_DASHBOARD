package com.sla.dashboard.service;

import com.sla.dashboard.entity.Job;
import com.sla.dashboard.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> getAllActiveJobs() {
        return jobRepository.findByActiveTrue();
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Optional<Job> getJobByName(String jobName) {
        return jobRepository.findByJobName(jobName);
    }

    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    @Transactional
    public Job saveJob(Job job) {
        log.info("Saving job: {}", job.getJobName());
        return jobRepository.save(job);
    }

    public Optional<Job> findMatchingJob(String emailSubject) {
        List<Job> activeJobs = getAllActiveJobs();
        for (Job job : activeJobs) {
            if (emailSubject.toLowerCase().contains(job.getEmailSubjectPattern().toLowerCase())) {
                return Optional.of(job);
            }
        }
        return Optional.empty();
    }
}
