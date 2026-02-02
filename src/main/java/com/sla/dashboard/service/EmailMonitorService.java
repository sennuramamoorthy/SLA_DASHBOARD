package com.sla.dashboard.service;

import com.sla.dashboard.entity.Job;
import jakarta.mail.*;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.ReceivedDateTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class EmailMonitorService {

    private static final Logger log = LoggerFactory.getLogger(EmailMonitorService.class);

    private final JobService jobService;
    private final JobExecutionService executionService;

    @Value("${email.imap.host}")
    private String imapHost;

    @Value("${email.imap.port}")
    private int imapPort;

    @Value("${email.imap.username}")
    private String username;

    @Value("${email.imap.password}")
    private String password;

    @Value("${email.imap.folder:INBOX}")
    private String folderName;

    @Value("${email.imap.ssl:true}")
    private boolean useSsl;

    public EmailMonitorService(JobService jobService, JobExecutionService executionService) {
        this.jobService = jobService;
        this.executionService = executionService;
    }

    public void checkEmails() {
        log.info("Starting email check...");
        Store store = null;
        Folder folder = null;

        try {
            Properties props = new Properties();
            if (useSsl) {
                props.put("mail.store.protocol", "imaps");
                props.put("mail.imaps.host", imapHost);
                props.put("mail.imaps.port", String.valueOf(imapPort));
                props.put("mail.imaps.ssl.enable", "true");
                props.put("mail.imaps.ssl.trust", "*");
            } else {
                props.put("mail.store.protocol", "imap");
                props.put("mail.imap.host", imapHost);
                props.put("mail.imap.port", String.valueOf(imapPort));
            }

            Session session = Session.getInstance(props);
            store = session.getStore(useSsl ? "imaps" : "imap");
            store.connect(imapHost, imapPort, username, password);

            folder = store.getFolder(folderName);
            folder.open(Folder.READ_ONLY);

            LocalDate today = LocalDate.now();
            Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            ReceivedDateTerm dateTerm = new ReceivedDateTerm(ComparisonTerm.GE, todayStart);

            Message[] messages = folder.search(dateTerm);
            log.info("Found {} emails for today", messages.length);

            processMessages(messages, today);

        } catch (MessagingException e) {
            log.error("Error checking emails: {}", e.getMessage(), e);
        } finally {
            closeResources(folder, store);
        }
    }

    private void processMessages(Message[] messages, LocalDate date) {
        for (Message message : messages) {
            try {
                String subject = message.getSubject();
                Date receivedDate = message.getReceivedDate();

                if (subject == null || receivedDate == null) {
                    continue;
                }

                LocalDateTime receivedDateTime = receivedDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                if (!receivedDateTime.toLocalDate().equals(date)) {
                    continue;
                }

                log.debug("Processing email: '{}' received at {}", subject, receivedDateTime);

                Optional<Job> matchingJob = jobService.findMatchingJob(subject);
                if (matchingJob.isPresent()) {
                    Job job = matchingJob.get();
                    var existingExecution = executionService.getExecutionForJobAndDate(job, date);

                    if (existingExecution.isEmpty() ||
                            existingExecution.get().getCompletionTime() == null) {
                        executionService.recordCompletion(job, date, receivedDateTime, subject);
                        log.info("Matched email '{}' to job '{}'", subject, job.getJobName());
                    }
                }

            } catch (MessagingException e) {
                log.error("Error processing message: {}", e.getMessage());
            }
        }
    }

    private void closeResources(Folder folder, Store store) {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (MessagingException e) {
            log.error("Error closing mail resources: {}", e.getMessage());
        }
    }
}
