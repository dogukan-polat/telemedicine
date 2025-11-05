# Telemedicine Platform API

A comprehensive telemedicine platform API built with Spring Boot that enables secure patient-doctor interactions, appointment management, and AI-powered symptom triage services.

## Features

- 🔐 **JWT-based Authentication & Authorization** - Secure user authentication with role-based access control
- 👥 **Multi-role User Management** - Support for Patients, Doctors, and Admins
- 📅 **Appointment Scheduling** - Complete appointment lifecycle management
- 🤖 **AI-Powered Triage** - Intelligent symptom analysis using Google Gemini AI
- 📧 **Email Notifications** - Automated email notifications for appointments
- 📊 **Admin Dashboard** - System statistics and audit trail management
- 📝 **API Documentation** - Interactive Swagger/OpenAPI documentation

## Tech Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 25
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT (jjwt 0.13.0)
- **AI Integration**: Google Gemini API 1.23.0
- **Email**: Spring Boot Starter Mail (Gmail SMTP)
- **Migration**: Flyway 11.13.1
- **Mapping**: MapStruct 1.6.3
- **Validation**: Spring Boot Starter Validation
- **Documentation**: SpringDoc OpenAPI 2.8.13
- **Configuration**: Spring DotEnv 4.0.0
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Spring Boot Test

## Prerequisites

- Java 17 or higher (Java 25 recommended)
- PostgreSQL 12 or higher
- Maven 3.6+
- Gmail account (for email notifications)
- Google Gemini API key

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd telemedicine
```

### 2. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE telemedicine;
```

The database schema will be automatically created by Flyway migrations when you first run the application.

#### Database Migrations

The project uses Flyway for database version control. Migrations are located in `src/main/resources/db/migration/`:

- **V1__initial_migration.sql** - Creates the `users` table with UUID support
- **V2__add_patients_and_doctors.sql** - Creates `patients` and `doctors` tables
- **V3__add_appointments.sql** - Creates `appointments` table
- **V4__add_ai_triage_audits.sql** - Creates `ai_triage_audits` table

Migrations run automatically on application startup. To manually run Flyway commands:

```bash
# Run migrations
mvn flyway:migrate

# Get migration status
mvn flyway:info

# Clean database (⚠️ WARNING: Deletes all data)
mvn flyway:clean

# Repair migration history
mvn flyway:repair
```

**Note**: Update database credentials in `pom.xml` under the `flyway-maven-plugin` configuration if needed.

### 3. Environment Configuration

Create a `.env` file in the project root (use `.env.example` as template):

```properties
JWT_SECRET=your-secret-key-at-least-32-characters
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-app-specific-password
GEMINI_API_KEY=your-gemini-api-key
```

**Note**: For Gmail, you need to generate an [App Password](https://support.google.com/accounts/answer/185833) instead of using your regular password.

### 4. Application Configuration

Update `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/telemedicine
    username: postgres
    password: your-password
```

### 5. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 6. Access API Documentation

Once the application is running, visit:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Docs: `http://localhost:8080/v3/api-docs`

## API Overview

### Authentication Endpoints

| Method | Endpoint                  | Description         | Auth Required |
|--------|---------------------------|---------------------|---------------|
| POST   | `/auth/login`             | User login          | No            |
| POST   | `/users/register/doctor`  | Register as doctor  | No            |
| POST   | `/users/register/patient` | Register as patient | No            |
| POST   | `/users/register/admin`   | Register as admin   | No*           |

*Admin registration requires email to match configured admin email

### Appointment Endpoints

| Method | Endpoint                      | Description              | Role          |
|--------|-------------------------------|--------------------------|---------------|
| GET    | `/appointments/patient/{id}`  | Get patient appointments | Authenticated |
| GET    | `/appointments/doctor/{id}`   | Get doctor appointments  | Authenticated |
| POST   | `/appointments`               | Create appointment       | DOCTOR        |
| PATCH  | `/appointments/{id}/cancel`   | Cancel appointment       | Authenticated |
| PATCH  | `/appointments/{id}/confirm`  | Confirm appointment      | Authenticated |
| PATCH  | `/appointments/{id}/complete` | Complete appointment     | Authenticated |
| DELETE | `/appointments/{id}`          | Delete appointment       | ADMIN         |

### AI Triage Endpoints

| Method | Endpoint                   | Description                    | Role   |
|--------|----------------------------|--------------------------------|--------|
| POST   | `/ai/triage/analyze`       | Analyze patient symptoms       | DOCTOR |
| POST   | `/ai/triage/quick-analyze` | Quick analysis (no patient ID) | DOCTOR |

### Admin Endpoints

| Method | Endpoint                               | Description               |
|--------|----------------------------------------|---------------------------|
| GET    | `/admin/users`                         | Get all users             |
| GET    | `/admin/patients`                      | Get all patients          |
| GET    | `/admin/doctors`                       | Get all doctors           |
| GET    | `/admin/stats`                         | Get system statistics     |
| GET    | `/admin/triages`                       | Get AI triage audits      |
| GET    | `/admin/triage-audits/patient/{id}`    | Get patient triage audits |
| GET    | `/admin/triage-audits/urgency/{level}` | Get audits by urgency     |
| PATCH  | `/admin/users/{email}/activate`        | Activate user             |
| PATCH  | `/admin/users/{email}/deactivate`      | Deactivate user           |
| PATCH  | `/admin/doctors/{license}/verify`      | Verify doctor             |
| PATCH  | `/admin/doctors/{license}/unverify`    | Unverify doctor           |
| DELETE | `/admin/users/{email}`                 | Delete user               |

## User Roles

### PATIENT
- Book and manage appointments
- Use AI triage services (via doctor)
- View personal appointment history

### DOCTOR
- Create and manage appointments
- Access AI symptom triage tools
- View patient appointment history
- Analyze patient symptoms

### ADMIN
- Full system access
- User management (activate/deactivate/delete)
- Doctor verification
- View system statistics
- Access AI triage audit logs

## Database Schema

The application uses Flyway migrations to manage the database schema. The following tables are created:

### Core Tables

**users** (V1__initial_migration.sql)
- Core user information for all user types
- Fields: id (UUID), email, password (encrypted), role, first_name, last_name, phone_number, is_active, created_at, updated_at
- Unique constraint on email
- Uses UUID extension for primary keys

**patients** (V2__add_patients_and_doctors.sql)
- Patient-specific data
- Fields: id (UUID), user_id (FK to users), emergency_contact_name, emergency_contact_phone, blood_type, allergies (array), created_at
- Blood type validation: A+, A-, B+, B-, AB+, AB-, O+, O-
- CASCADE delete on user deletion

**doctors** (V2__add_patients_and_doctors.sql)
- Doctor-specific data
- Fields: id (UUID), user_id (FK to users), medical_license_number (unique), specialization, years_of_experience, biography, consultation_fee, is_verified, created_at
- CASCADE delete on user deletion

**appointments** (V3__add_appointments.sql)
- Appointment scheduling and management
- Fields: id (UUID), patient_id (FK), doctor_id (FK), scheduled_date, scheduled_time, duration_minutes, status, created_at
- Status values: SCHEDULED, CANCELLED, CONFIRMED, COMPLETED
- CASCADE delete on patient/doctor deletion

**ai_triage_audits** (V4__add_ai_triage_audits.sql)
- AI analysis audit trail
- Fields: id (UUID), patient_id (FK), user_input (symptoms), ai_output (analysis), urgency_level, created_at
- Urgency levels: LOW, MEDIUM, HIGH, EMERGENCY
- CASCADE delete on patient deletion

**doctors_availability** (V5__add_doctor_availability.sql)
- Doctor availability management
- Fields: id (UUID), doctor_id (FK), day_of_week, start_time, end_time, is_available, created_at
- Day of week values: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
- Time format: HH:mm:ss
- Unique constraint on doctor_id & day_of_week
- CASCADE delete on doctor deletion

### Database Relationships

```
users (1) ←→ (1) patients
users (1) ←→ (1) doctors
patients (1) ←→ (∞) appointments
doctors (1) ←→ (∞) appointments
patients (1) ←→ (∞) ai_triage_audits
doctors (1) ←→ (1) doctors_availability
```

### Migration Management

To view the current migration status:

```bash
mvn flyway:info
```

To apply pending migrations:

```bash
mvn flyway:migrate
```

All migrations are idempotent and safe to run multiple times.

## Project Structure

```
telemedicine/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/dogukanpolat/telemedicine/
│   │   │       ├── config/            # Configuration(Swagger UI)
│   │   │       ├── controller/        # REST controllers
│   │   │       ├── dto/               # Data Transfer Objects
│   │   │       ├── exception/         # Custom exceptions
│   │   │       ├── mappers/           # MapStruct mappers
│   │   │       ├── model/             # JPA entities
│   │   │       ├── repository/        # Spring Data repositories
│   │   │       ├── security/          # Security configuration
│   │   │       └── service/           # Business logic
│   │   └── resources/
│   │       ├── db/migration/          # Flyway migrations
│   │       └── application.yml        # Application configuration
│   └── test/                          # Test files
├── .env                               # Environment variables
├── .env.example                       # Example for .env file
├── Dockerfile                         # Dockerfile for deployment
├── docker-compose.yml                 # Docker Compose file for local development
├── .gitignore
├── pom.xml                            # Maven configuration
└── README.md
```

## AI Triage System

The AI triage system uses Google Gemini to analyze patient symptoms and provides:
- **Urgency Level**: LOW, MEDIUM, HIGH, EMERGENCY
- **Recommended Specialty**: Suggested medical specialty
- **Key Questions**: Follow-up questions for doctors
- **Triage Notes**: Analysis summary
- **Audit Trail**: All analyses are logged for review

### Example Triage Request

```json
{
  "patientId": "uuid-here",
  "symptomDescription": "Patient experiencing chest pain and shortness of breath"
}
```

### Example Response

```json
{
  "patientId": "uuid-here",
  "urgency": "EMERGENCY",
  "recommendedSpecialty": "Cardiology",
  "keyQuestions": [
    "When did symptoms start?",
    "Any history of heart disease?"
  ],
  "triageNotes": "Severe symptoms requiring immediate attention",
  "disclaimer": "AI analysis for triage assistance only. Not medical advice."
}
```

## Email Notifications

The system automatically sends email notifications for:
- Appointment confirmations (to both patient and doctor)
- Appointment cancellations
- Status updates

## Doctor Availability Management

The system includes comprehensive availability management that allows doctors to define their working hours and ensures appointments can only be booked during available time slots.

### Availability Endpoints

| Method | Endpoint                                  | Description                        | Role   |
|--------|-------------------------------------------|------------------------------------|--------|
| POST   | `/availability`                           | Create availability slot           | DOCTOR |
| POST   | `/availability/bulk`                      | Create multiple availability slots | DOCTOR |
| GET    | `/availability/doctor/{id}`               | Get doctor's availability          | Auth   |
| GET    | `/availability/doctor/{id}/day/{day}`     | Get availability by day            | Auth   |
| GET    | `/availability/doctor/{id}/active`        | Get active availability only       | Auth   |
| PATCH  | `/availability/{id}`                      | Update availability slot           | DOCTOR |
| DELETE | `/availability/{id}`                      | Delete availability slot           | DOCTOR |
| DELETE | `/availability/doctor/{id}`               | Delete all doctor's availability   | DOCTOR |

### Features

- **Time Slot Management**: Doctors can define their available hours for each day of the week
- **Overlap Prevention**: System prevents creating overlapping availability slots
- **Appointment Validation**: Appointments can only be created during doctor's available hours
- **Flexible Scheduling**: Support for different schedules on different days
- **Bulk Operations**: Create multiple availability slots at once
- **Enable/Disable Slots**: Temporarily disable availability without deletion

### Example: Creating Availability

**Single Slot:**

POST /availability
```json
{
  "doctorId": "uuid-here",
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "17:00:00"
}
```

**Bulk Creation:**

POST /availability/bulk

```json

{
  "doctorId": "uuid-here",
  "slots": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "09:00:00",
      "endTime": "12:00:00"
    },
    {
      "dayOfWeek": "MONDAY",
      "startTime": "13:00:00",
      "endTime": "17:00:00"
    },
    {
      "dayOfWeek": "TUESDAY",
      "startTime": "09:00:00",
      "endTime": "17:00:00"
    }
  ]
}
```

### Example: Updating Availability  

PATCH /availability/{availability-id}

```json
{
  "startTime": "10:00:00",
  "endTime": "18:00:00",
  "isAvailable": true
}
```

### Appointment Booking with Availability

When creating an appointment, the system automatically:
1. Checks if the doctor has availability defined for that day
2. Validates that the appointment time falls within an available slot
3. Rejects the appointment if the doctor is not available
4. Returns a clear error message if validation fails

**Error Response:**
```json
{
  "status": "400",
  "error": "Availability Error",
  "message": "Doctor is not available at the requested time. Please check doctor's availability schedule.",
  "timestamp": "2025-10-26T10:30:00"
}
```

### Days of Week

Valid values for `dayOfWeek`:
- MONDAY
- TUESDAY
- WEDNESDAY
- THURSDAY
- FRIDAY
- SATURDAY
- SUNDAY

### Time Format

Times should be in ISO-8601 format: `HH:mm:ss` (e.g., "09:00:00", "17:30:00")

### Validation Rules

1. **Time Range**: Start time must be before end time
2. **No Overlaps**: Cannot create overlapping availability slots for the same day
3. **Doctor Exists**: Doctor ID must reference an existing doctor
4. **Active Slots**: Only active slots (isAvailable=true) are considered for appointment validation

### Use Cases

**Morning/Afternoon Split:**
```json
[
  {
    "dayOfWeek": "MONDAY",
    "startTime": "08:00:00",
    "endTime": "12:00:00"
  },
  {
    "dayOfWeek": "MONDAY",
    "startTime": "14:00:00",
    "endTime": "18:00:00"
  }
]
```

**Weekend Coverage:**
```json
[
  {
    "dayOfWeek": "SATURDAY",
    "startTime": "09:00:00",
    "endTime": "13:00:00"
  }
]
```

**Emergency Coverage (24/7):**
```json
[
  {
    "dayOfWeek": "MONDAY",
    "startTime": "00:00:00",
    "endTime": "23:59:59"
  }
]
```

## Testing

Run the test suite:

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

The project includes:
- Unit tests for services
- Integration tests for controllers
- Repository tests
- Security tests

## Security

- Passwords are encrypted using BCrypt
- JWT tokens expire after 24 hours (configurable)
- Role-based access control (RBAC)
- CSRF protection disabled for REST API
- Stateless session management

## Error Handling

The API uses standard HTTP status codes:
- `200` - Success
- `201` - Created
- `204` - No Content
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (authentication required)
- `403` - Forbidden (insufficient permissions)
- `500` - Internal Server Error

All errors return a consistent JSON format:

```json
{
  "status": "400",
  "error": "Error Type",
  "message": "Detailed error message",
  "timestamp": "2025-10-22T10:30:00"
}
```

## Configuration Properties

Key application properties in `application.yml`:

```yaml
jwt:
  secret: ${JWT_SECRET}      # JWT signing key
  expiration: 86400          # Token expiration (seconds)

gemini:
  api-key: ${GEMINI_API_KEY} # Google Gemini API key

spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

## Development Tips

1. **Database Migrations**: 
   - Flyway automatically runs migrations on startup
   - Never modify existing migration files
   - Create new migration files with format: `V{version}__{description}.sql`
   - Check migration status: `mvn flyway:info`
   
2. **API Testing**: Use Swagger UI for interactive API testing

3. **Logging**: Check console logs for detailed application flow

4. **Email Testing**: Use a test Gmail account for development

5. **Maven Compilation**: The project uses annotation processors for Lombok and MapStruct
   ```bash
   # Clean build
   mvn clean install
   
   # Skip tests during build
   mvn clean install -DskipTests
   ```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**Doğukan Polat**
- GitHub: [@dogukan-polat](https://github.com/dogukan-polat)