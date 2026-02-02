#!/bin/bash
# Script to simulate job completion emails for testing
# Usage: ./send-test-emails.sh [job_number]
# If no argument provided, sends all 10 job emails

SMTP_HOST="${SMTP_HOST:-localhost}"
SMTP_PORT="${SMTP_PORT:-3025}"

send_email() {
    local job_name="$1"
    local subject="$2"

    echo "Sending email for: $job_name"

    curl --silent --url "smtp://${SMTP_HOST}:${SMTP_PORT}" \
        --mail-from "jobs@company.com" \
        --mail-rcpt "test@test.com" \
        --upload-file - <<EOF
From: Job Scheduler <jobs@company.com>
To: SLA Monitor <test@test.com>
Subject: $subject
Date: $(date -R)
Content-Type: text/plain; charset=utf-8

Job completed successfully.

Job Name: $job_name
Status: SUCCESS
Timestamp: $(date)

This is an automated message from the job scheduler.
EOF

    if [ $? -eq 0 ]; then
        echo "  Done"
    else
        echo "  Failed"
    fi
}

echo "Sending test emails for all 10 jobs..."
echo "========================================"

send_email "Daily Sales Report" "Sales Report - $(date +%Y-%m-%d) - SUCCESS"
sleep 0.5
send_email "Inventory Sync" "Inventory Sync Complete - $(date +%Y-%m-%d)"
sleep 0.5
send_email "Customer Data ETL" "Customer ETL Job Finished Successfully"
sleep 0.5
send_email "Financial Reconciliation" "Financial Recon Complete - Daily Run"
sleep 0.5
send_email "Order Processing Batch" "Order Batch Complete - Processed 1523 orders"
sleep 0.5
send_email "Analytics Pipeline" "Analytics Pipeline Success - All metrics updated"
sleep 0.5
send_email "Backup Verification" "Backup Verified - All databases backed up"
sleep 0.5
send_email "Report Generation" "Reports Generated - 15 reports created"
sleep 0.5
send_email "Data Quality Check" "Data Quality Check Passed - 0 errors found"
sleep 0.5
send_email "End of Day Processing" "EOD Processing Complete - All tasks finished"

echo "========================================"
echo "Done! Emails sent to GreenMail server."
echo ""
echo "GreenMail API: http://localhost:8082"
echo "App API: http://localhost:8081"
