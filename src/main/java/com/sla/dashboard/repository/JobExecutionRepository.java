package com.sla.dashboard.repository;

import com.sla.dashboard.entity.Job;
import com.sla.dashboard.entity.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {

    List<JobExecution> findByExecutionDate(LocalDate date);

    Optional<JobExecution> findByJobAndExecutionDate(Job job, LocalDate date);

    List<JobExecution> findByExecutionDateOrderByJobJobName(LocalDate date);

    boolean existsByJobAndExecutionDate(Job job, LocalDate date);
}
