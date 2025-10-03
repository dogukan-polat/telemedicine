
CREATE TABLE doctors
(
    id                     UUID                    DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id                UUID           NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    medical_license_number VARCHAR(100)   NOT NULL UNIQUE,
    specialization         VARCHAR(100)   NOT NULL,
    years_of_experience    INTEGER        NOT NULL,
    biography              TEXT,
    consultation_fee       DECIMAL(10, 2) NOT NULL,
    is_verified            BOOLEAN                 DEFAULT TRUE,
    created_at             DATE           NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE patients
(
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id                 UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    emergency_contact_name  VARCHAR(200),
    emergency_contact_phone VARCHAR(20),
    blood_type              VARCHAR(5) CHECK (blood_type IN ('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-')),
    allergies               TEXT[],
    created_at              DATE             DEFAULT CURRENT_DATE
);