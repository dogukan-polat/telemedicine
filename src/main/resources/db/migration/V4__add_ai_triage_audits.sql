
CREATE TABLE ai_triage_audits(
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    patient_id uuid NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    user_input TEXT NOT NULL,
    ai_output TEXT NOT NULL,
    urgency_level VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);