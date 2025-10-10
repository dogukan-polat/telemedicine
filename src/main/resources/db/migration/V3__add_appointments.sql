CREATE TABLE appointments
(
    id               UUID PRIMARY KEY     DEFAULT uuid_generate_v4(),
    patient_id       UUID        NOT NULL REFERENCES patients (id) ON DELETE CASCADE,
    doctor_id        UUID        NOT NULL REFERENCES doctors (id) ON DELETE CASCADE,
    scheduled_date   DATE        NOT NULL,
    scheduled_time   TIME        NOT NULL,
    duration_minutes INT         NOT NULL DEFAULT 30,
    status           VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'CANCELLED', 'CONFIRMED', 'COMPLETED')),
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW()
);