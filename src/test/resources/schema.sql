CREATE TABLE IF NOT EXISTS bethel (
    bethel_id UUID PRIMARY KEY,
    number INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255),
    state VARCHAR(100),
    email VARCHAR(255),
    founding_date DATE,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS report_period (
    period_id UUID PRIMARY KEY,
    year INTEGER NOT NULL,
    description VARCHAR(255),
    opening_date DATE NOT NULL,
    closing_date DATE NOT NULL,
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS app_user (
    user_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    google_sub VARCHAR(255),
    invite_status VARCHAR(50),
    invite_sent_at TIMESTAMP,
    last_access_at TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS report_job (
    job_id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    format VARCHAR(10) NOT NULL,
    parameters JSONB,
    status VARCHAR(50) NOT NULL,
    file_key VARCHAR(500),
    error_message TEXT,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    supreme_email VARCHAR(255),
    sent_to_supreme_at TIMESTAMP,
    period_id UUID REFERENCES report_period(period_id),
    user_id UUID REFERENCES app_user(user_id)
);

CREATE TABLE IF NOT EXISTS member (
    member_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    birth_date DATE,
    initiation_date DATE,
    status VARCHAR(50),
    bethel_id UUID REFERENCES bethel(bethel_id)
);

CREATE TABLE IF NOT EXISTS activity (
    activity_id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    activity_date DATE NOT NULL,
    bethel_id UUID REFERENCES bethel(bethel_id)
);

CREATE TABLE IF NOT EXISTS bethel_term (
    bethel_term_id UUID PRIMARY KEY,
    bethel_id UUID REFERENCES bethel(bethel_id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS bethel_officer (
    bethel_officer_id UUID PRIMARY KEY,
    position VARCHAR(100) NOT NULL,
    can_submit_report BOOLEAN DEFAULT FALSE,
    start_date DATE,
    end_date DATE,
    member_id UUID REFERENCES member(member_id),
    bethel_term_id UUID REFERENCES bethel_term(bethel_term_id)
);

CREATE TABLE IF NOT EXISTS form (
    form_id UUID PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    version INTEGER DEFAULT 1,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS form_field (
    field_id UUID PRIMARY KEY,
    section VARCHAR(255),
    label VARCHAR(255),
    key VARCHAR(255),
    type VARCHAR(50),
    required BOOLEAN DEFAULT FALSE,
    financial BOOLEAN DEFAULT FALSE,
    validation_rules TEXT,
    options TEXT,
    sort_order INTEGER,
    form_id UUID REFERENCES form(form_id)
);

CREATE TABLE IF NOT EXISTS report (
    report_id UUID PRIMARY KEY,
    status VARCHAR(50),
    submitted_at TIMESTAMP,
    reopened_at TIMESTAMP,
    reopen_deadline TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    bethel_id UUID REFERENCES bethel(bethel_id),
    period_id UUID REFERENCES report_period(period_id),
    form_id UUID REFERENCES form(form_id)
);

CREATE TABLE IF NOT EXISTS report_answer (
    report_id UUID REFERENCES report(report_id),
    field_id UUID REFERENCES form_field(field_id),
    value TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (report_id, field_id)
);
