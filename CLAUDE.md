# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SLA Dashboard is a Java Spring Boot service that monitors email for job completion notifications and tracks SLA compliance. It reads emails via IMAP, matches them to configured jobs, and sends daily summary reports with color-coded status indicators.

## Build and Run Commands

```bash
# Build the application
mvn clean package

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=JobServiceTest

# Start with Docker Compose (includes PostgreSQL)
docker-compose up -d

# Start only the database
docker-compose up -d db

# Run locally (requires running PostgreSQL)
mvn spring-boot:run

# View logs
docker-compose logs -f app
```

## Architecture

### Core Flow
1. **Scheduler** (`SlaMonitorScheduler`) triggers email checks every 5 minutes
2. **EmailMonitorService** connects via IMAP, reads today's emails, matches subjects to job patterns
3. **JobExecutionService** records completions and calculates SLA compliance
4. **SummaryEmailService** sends HTML summary email at configured time (default 6 PM)

### Key Components
- `entity/Job` - Job configuration (name, email pattern, SLA time)
- `entity/JobExecution` - Daily execution record with status (PENDING/COMPLETED/MISSED)
- `service/EmailMonitorService` - IMAP email reading and pattern matching
- `service/SummaryEmailService` - HTML summary generation with Thymeleaf template
- `scheduler/SlaMonitorScheduler` - Cron-based task scheduling
- `controller/SlaController` - REST API for manual triggers and status queries

### Database Schema
- `jobs` - Job definitions with SLA times
- `job_executions` - Per-day execution status tracking

## Configuration

Environment variables (see `.env.example`):
- `IMAP_*` - Email server for reading job notifications
- `SMTP_*` - Email server for sending summaries
- `SUMMARY_RECIPIENT` - Who receives the daily report
- `SUMMARY_CRON` - When to send (default: `0 0 18 * * ?` = 6 PM)

## REST API Endpoints

- `GET /api/jobs` - List all jobs
- `GET /api/executions?date=YYYY-MM-DD` - Get day's executions
- `GET /api/stats?date=YYYY-MM-DD` - Get SLA statistics
- `POST /api/actions/check-emails` - Trigger email check
- `POST /api/actions/send-summary` - Trigger summary email
- `POST /api/actions/initialize` - Initialize day's executions
