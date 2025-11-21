
CREATE TABLE medical_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    doctor_id           UUID REFERENCES doctors (id) ON DELETE SET NULL,
    appointment_id      UUID REFERENCES appointments (id) ON DELETE SET NULL,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    record_type         VARCHAR(50)  NOT NULL,
    file_name           VARCHAR(255) NOT NULL,
    file_path           VARCHAR(500) NOT NULL,
    file_size           BIGINT,
    mime_type           VARCHAR(100),
    is_encrypted        BOOLEAN      NOT NULL DEFAULT TRUE,
    version             INTEGER      NOT NULL DEFAULT 1,
    previous_version_id UUID REFERENCES medical_records (id) ON DELETE SET NULL,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    record_date         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    uploaded_by         UUID REFERENCES users (id) ON DELETE SET NULL,
    notes               TEXT
)