
CREATE TABLE doctor_availability
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id     UUID        NOT NULL REFERENCES doctors (id) ON DELETE CASCADE,
    day_of_week   VARCHAR(10) NOT NULL CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    start_time    TIME        NOT NULL,
    end_time      TIME        NOT NULL,
    is_available  BOOLEAN              DEFAULT TRUE,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT valid_time_range CHECK (end_time > start_time),
    CONSTRAINT no_overlap UNIQUE (doctor_id, day_of_week, start_time, end_time)
);

CREATE INDEX idx_doctor_availability_doctor_id ON doctor_availability (doctor_id);
CREATE INDEX idx_doctor_availability_day ON doctor_availability (day_of_week);