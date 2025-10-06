# Telemedicine Application

A comprehensive telemedicine platform built with Spring Boot that enables secure patient-doctor interactions, appointment management, and healthcare delivery through digital channels.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Security](#security)
- [License](#license)

## ✨ Features

- **User Management**
  - Separate registration for doctors and patients
  - JWT-based authentication and authorization
  - Role-based access control (PATIENT, DOCTOR, ADMIN)

- **Doctor Features**
  - Medical license verification
  - Specialization management
  - Consultation fee configuration
  - Professional biography

- **Patient Features**
  - Emergency contact information
  - Blood type and allergy tracking
  - Medical history management

- **Security**
  - Password encryption with BCrypt
  - JWT token-based authentication
  - Secure REST API endpoints

## 🛠 Tech Stack

- **Framework:** Spring Boot 3.5.6
- **Language:** Java 25
- **Database:** PostgreSQL
- **Migration:** Flyway
- **Security:** Spring Security + JWT (jjwt 0.13.0)
- **ORM:** Spring Data JPA + Hibernate
- **Validation:** Jakarta Bean Validation
- **Documentation:** SpringDoc OpenAPI 3
- **Build Tool:** Maven
- **Utilities:** 
  - Lombok
  - MapStruct 1.6.3
  - Spring DotEnv 4.0.0

## 📦 Prerequisites

Before running this application, ensure you have:

- Java 25
- Maven 3.6+
- PostgreSQL 12+
- Git

## 🚀 Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd telemedicine
```

2. **Create PostgreSQL database**
```sql
CREATE DATABASE telemedicine;
```

3. **Create `.env` file in the project root**
```env
JWT_SECRET=your-secret-key-here-make-it-at-least-256-bits-long
```

4. **Build the project**
```bash
mvn clean install
```

## ⚙️ Configuration

### Application Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/telemedicine
    username: postgres
    password: your-password
  jpa:
    show-sql: true

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400  # 24 hours in seconds
```

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `JWT_SECRET` | Secret key for JWT token generation (min 256 bits) | Yes |

## 🗄️ Database Setup

The application uses Flyway for database migrations. Migrations are automatically applied on startup.

### Manual Migration

If you need to run migrations manually:

```bash
mvn flyway:migrate
```

### Database Schema

The application creates the following tables:
- `users` - Base user information
- `doctors` - Doctor-specific information
- `patients` - Patient-specific information

## 🏃 Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

### Production Mode

```bash
java -jar target/telemedicine-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, access the Swagger UI documentation at:

```
http://localhost:8080/swagger-ui.html
```

Documentation's details will be added later.

### API Endpoints

#### Authentication

**Register Doctor**
```http
POST /auth/doctor/register
Content-Type: application/json

{
  "user": {
    "email": "doctor@example.com",
    "password": {
      "password": "securePassword123",
      "confirmPassword": "securePassword123"
    },
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+905551234567",
    "dateOfBirth": "1985-05-15"
  },
  "medicalLicenseNumber": "MD123456",
  "specialization": "Cardiology",
  "yearsOfExperience": 10,
  "biography": "Experienced cardiologist...",
  "consultationFee": 150.00
}
```

**Register Patient**
```http
POST /auth/patient/register
Content-Type: application/json

{
  "user": {
    "email": "patient@example.com",
    "password": {
      "password": "securePassword123",
      "confirmPassword": "securePassword123"
    },
    "firstName": "Jane",
    "lastName": "Smith",
    "phoneNumber": "+905559876543",
    "dateOfBirth": "1990-08-20"
  },
  "emergencyContactName": "John Smith",
  "emergencyContactPhone": "+905551234567",
  "bloodType": "A+",
  "allergies": ["Penicillin", "Peanuts"]
}
```

**Login**
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Authentication

For protected endpoints, include the JWT token in the Authorization header:

```http
Authorization: Bearer <your-jwt-token>
```

## 📁 Project Structure

```
telemedicine/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/dogukanpolat/telemedicine/
│   │   │       ├── controller/         # REST controllers
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
├── .gitignore
├── pom.xml                            # Maven configuration
└── README.md
```

## 🔒 Security

### Password Requirements
- Minimum 8 characters
- Maximum 20 characters
- Must be confirmed during registration

### JWT Token
- Expires after 24 hours (configurable)
- Uses HS256 algorithm
- Stateless authentication

### Validation Rules

**Email:** Must be valid email format

**Phone Number:** Must match pattern `^\+?[1-9][0-9]\d{1,14}`

**Blood Type:** Must be one of: A+, A-, B+, B-, AB+, AB-, O+, O-

**Medical License:** Maximum 100 characters, required for doctors

**Consultation Fee:** Must be non-negative decimal


## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👤 Author

Doğukan Polat

## ⚠️ Known Issues

- None at the moment

## 📊 Database ER Diagram

```
users (1) ←→ (1) doctors
users (1) ←→ (1) patients
```

---

**Note:** This is a work in progress. Features and documentation will be updated regularly.
