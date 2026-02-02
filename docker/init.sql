-- Sample data for SLA Dashboard
-- This script runs on first database initialization

-- Create the jobs table
CREATE TABLE IF NOT EXISTS jobs (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(255) NOT NULL UNIQUE,
    email_subject_pattern VARCHAR(255) NOT NULL,
    sla_time TIME NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT true
);

-- Create the job_executions table
CREATE TABLE IF NOT EXISTS job_executions (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL REFERENCES jobs(id),
    execution_date DATE NOT NULL,
    completion_time TIMESTAMP,
    email_subject VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    within_sla BOOLEAN,
    sla_difference_minutes BIGINT,
    UNIQUE(job_id, execution_date)
);

-- Insert 10 sample jobs with their SLA times
INSERT INTO jobs (job_name, email_subject_pattern, sla_time, description, active) VALUES
('Daily Sales Report', 'Sales Report', '06:00:00', 'Daily sales aggregation report from CRM system', true),
('Inventory Sync', 'Inventory Sync Complete', '07:00:00', 'Synchronizes inventory data with warehouse system', true),
('Customer Data ETL', 'Customer ETL', '08:00:00', 'Extracts and transforms customer data from multiple sources', true),
('Financial Reconciliation', 'Financial Recon', '09:00:00', 'Daily financial reconciliation job', true),
('Order Processing Batch', 'Order Batch Complete', '10:00:00', 'Processes pending orders from the queue', true),
('Analytics Pipeline', 'Analytics Pipeline Success', '11:00:00', 'Runs analytics aggregations for dashboards', true),
('Backup Verification', 'Backup Verified', '12:00:00', 'Verifies database backup completion', true),
('Report Generation', 'Reports Generated', '14:00:00', 'Generates scheduled business reports', true),
('Data Quality Check', 'Data Quality', '15:00:00', 'Runs data quality validation rules', true),
('End of Day Processing', 'EOD Processing Complete', '17:00:00', 'End of day batch processing and cleanup', true);
