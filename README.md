# SLA Dashboard

A Java Spring Boot service that monitors email for job completion notifications and tracks SLA (Service Level Agreement) compliance. It reads emails via IMAP, matches them to configured jobs, and sends daily summary reports with color-coded status indicators.

## Features

- **Email Monitoring**: Automatically checks inbox via IMAP for job completion emails
- **SLA Tracking**: Compares job completion time against configured SLA deadlines
- **Daily Summary Reports**: Sends HTML email with color-coded job status
- **REST API**: Endpoints for manual triggers and status queries
- **Database Storage**: Persists job configurations and daily execution history
- **Docker Support**: Includes Docker Compose setup with PostgreSQL and test mail server

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Email Inbox   │────▶│  SLA Dashboard  │────▶│   PostgreSQL    │
│   (IMAP)        │     │  (Spring Boot)  │     │   Database      │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │  Summary Email  │
                        │  (SMTP)         │
                        └─────────────────┘
```

### Core Components

| Component | Description |
|-----------|-------------|
| `EmailMonitorService` | Connects to IMAP server, reads emails, matches subjects to jobs |
| `JobExecutionService` | Records completions, calculates SLA compliance |
| `SummaryEmailService` | Generates and sends HTML summary reports |
| `SlaMonitorScheduler` | Runs scheduled tasks (email check, daily summary) |

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for containerized deployment)
- Email account with IMAP/SMTP access (or use included test mail server)

## Quick Start

### Using Docker Compose (Recommended)

```bash
# Start all services (PostgreSQL + GreenMail + App)
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down -v
```

**Services:**
| Service | Port | Description |
|---------|------|-------------|
| App | 8081 | SLA Dashboard API |
| PostgreSQL | 5432 | Database |
| GreenMail | 3025 (SMTP), 3143 (IMAP) | Test mail server |

### Local Development

```bash
# Start PostgreSQL
docker run -d --name sla-postgres \
  -e POSTGRES_DB=sladb \
  -e POSTGRES_USER=slauser \
  -e POSTGRES_PASSWORD=slapass \
  -p 5432:5432 \
  postgres:15-alpine

# Build and run
mvn clean package -DskipTests
java -jar target/sla-dashboard-1.0.0.jar
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | localhost |
| `DB_PORT` | PostgreSQL port | 5432 |
| `DB_NAME` | Database name | sladb |
| `DB_USERNAME` | Database user | slauser |
| `DB_PASSWORD` | Database password | slapass |
| `IMAP_HOST` | IMAP server host | imap.gmail.com |
| `IMAP_PORT` | IMAP server port | 993 |
| `IMAP_USERNAME` | IMAP username | - |
| `IMAP_PASSWORD` | IMAP password | - |
| `IMAP_SSL` | Use SSL for IMAP | true |
| `SMTP_HOST` | SMTP server host | smtp.gmail.com |
| `SMTP_PORT` | SMTP server port | 587 |
| `SMTP_USERNAME` | SMTP username | - |
| `SMTP_PASSWORD` | SMTP password | - |
| `SUMMARY_RECIPIENT` | Email recipient for daily summary | - |
| `SUMMARY_FROM` | From address for summary emails | noreply@sla-dashboard.local |
| `SUMMARY_CRON` | Cron expression for daily summary | 0 0 18 * * ? (6 PM) |
| `EMAIL_CHECK_INTERVAL` | Email check interval in ms | 300000 (5 min) |
| `SCHEDULER_ENABLED` | Enable/disable scheduler | true |

### Gmail Configuration

For Gmail, use an [App Password](https://support.google.com/accounts/answer/185833):

```bash
export IMAP_HOST=imap.gmail.com
export IMAP_PORT=993
export IMAP_USERNAME=your-email@gmail.com
export IMAP_PASSWORD=your-app-password
export SMTP_HOST=smtp.gmail.com
export SMTP_PORT=587
export SMTP_USERNAME=your-email@gmail.com
export SMTP_PASSWORD=your-app-password
```

## REST API

### Jobs

```bash
# List all jobs
curl http://localhost:8081/api/jobs

# List active jobs
curl http://localhost:8081/api/jobs/active

# Create a job
curl -X POST http://localhost:8081/api/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "jobName": "My Job",
    "emailSubjectPattern": "My Job Complete",
    "slaTime": "10:00:00",
    "description": "My scheduled job",
    "active": true
  }'
```

### Executions & Stats

```bash
# Get today's executions
curl http://localhost:8081/api/executions

# Get executions for specific date
curl http://localhost:8081/api/executions?date=2024-01-15

# Get SLA statistics
curl http://localhost:8081/api/stats
curl http://localhost:8081/api/stats?date=2024-01-15
```

### Manual Actions

```bash
# Initialize today's job executions
curl -X POST http://localhost:8081/api/actions/initialize

# Trigger email check
curl -X POST http://localhost:8081/api/actions/check-emails

# Send summary email
curl -X POST http://localhost:8081/api/actions/send-summary

# Mark missed jobs
curl -X POST http://localhost:8081/api/actions/mark-missed
```

## Sample Jobs

The database is pre-loaded with 10 sample jobs:

| Job Name | SLA Time | Email Pattern |
|----------|----------|---------------|
| Daily Sales Report | 06:00 | Sales Report |
| Inventory Sync | 07:00 | Inventory Sync Complete |
| Customer Data ETL | 08:00 | Customer ETL |
| Financial Reconciliation | 09:00 | Financial Recon |
| Order Processing Batch | 10:00 | Order Batch Complete |
| Analytics Pipeline | 11:00 | Analytics Pipeline Success |
| Backup Verification | 12:00 | Backup Verified |
| Report Generation | 14:00 | Reports Generated |
| Data Quality Check | 15:00 | Data Quality |
| End of Day Processing | 17:00 | EOD Processing Complete |

## Testing with Simulated Emails

Use the included script to send test job completion emails:

```bash
# Send all 10 job completion emails
./docker/send-test-emails.sh

# Send specific job email (1-10)
./docker/send-test-emails.sh 1
```

Then trigger email check:

```bash
curl -X POST http://localhost:8081/api/actions/check-emails
```

## Summary Email

The daily summary email includes:

- **Header**: Date and overall status
- **Statistics**: Total jobs, completed, within SLA, late, missed, pending
- **SLA Compliance**: Percentage of jobs meeting SLA
- **Job Details Table**: Each job with completion time, SLA difference, and status

### Color Coding

| Color | Status | Meaning |
|-------|--------|---------|
| 🟢 Green | ON TIME | Completed before SLA deadline |
| 🟠 Orange | LATE | Completed after SLA deadline |
| 🔴 Red | MISSED | No completion email received |
| ⚪ Gray | PENDING | SLA deadline not yet reached |

## Project Structure

```
SLA_Dashboard/
├── pom.xml                           # Maven configuration
├── Dockerfile                        # Docker build file
├── docker-compose.yml                # Docker Compose setup
├── docker/
│   ├── init.sql                      # Database initialization
│   └── send-test-emails.sh           # Test email script
└── src/main/java/com/sla/dashboard/
    ├── SlaDashboardApplication.java  # Main application
    ├── entity/
    │   ├── Job.java                  # Job configuration entity
    │   └── JobExecution.java         # Daily execution record
    ├── repository/
    │   ├── JobRepository.java
    │   └── JobExecutionRepository.java
    ├── service/
    │   ├── JobService.java           # Job management
    │   ├── JobExecutionService.java  # Execution tracking
    │   ├── EmailMonitorService.java  # IMAP email reading
    │   └── SummaryEmailService.java  # Summary email sender
    ├── scheduler/
    │   └── SlaMonitorScheduler.java  # Scheduled tasks
    └── controller/
        └── SlaController.java        # REST API
```

## How It Works

1. **Midnight**: Scheduler initializes PENDING executions for all active jobs
2. **Every 5 minutes**: Scheduler checks inbox for new emails
3. **Email Processing**:
   - Reads today's emails from IMAP inbox
   - Matches email subjects against job patterns
   - Records completion time and calculates SLA compliance
4. **Hourly**: Scheduler marks jobs as MISSED if SLA deadline passed without completion
5. **6 PM (configurable)**: Scheduler sends daily summary email

## License

MIT
