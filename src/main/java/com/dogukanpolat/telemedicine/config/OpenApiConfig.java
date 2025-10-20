package com.dogukanpolat.telemedicine.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Telemedicine API",
                version = "1.0.0",
                description = """
                        A comprehensive telemedicine platform API that enables secure patient-doctor interactions,
                        appointment management, and AI-powered triage services.
                       
                        ## Features
                        - User authentication and authorization (JWT-based)
                        - Doctor and patient registration
                        - Appointment scheduling and management
                        - AI-powered symptom triage
                        - Admin dashboard for system management
                        - Email notifications
                       
                        ## Authentication
                        Most endpoints require JWT authentication. To authenticate:
                        1. Register as a doctor or patient
                        2. Login to receive a JWT token
                        3. Add an authorization header and enter: `Bearer <your-token>`
                       
                        ## Roles
                        - **PATIENT**: Can book appointments, use triage services
                        - **DOCTOR**: Can manage appointments, access triage tools
                        - **ADMIN**: Full system access and user management
                       """,
                contact = @Contact(
                        name = "Doğukan Polat",
                        url= "https://github.com/dogukanpolat"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development Server",
                        url = "http://localhost:8080"
                )
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT authentication. Format: Bearer <token>",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
