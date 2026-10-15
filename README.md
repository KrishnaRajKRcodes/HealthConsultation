# HealthConnect — Online Health Consultation Platform

A Java web application that brings patients, doctors, and administrators together in one place for appointment booking, consultation advice, health records, messaging, scheduling, and platform administration.

**Technology:** Java · Servlets · JSP/JSTL · JDBC · MySQL · Maven · Apache Tomcat 9

---

## Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Problems Addressed](#problems-addressed)
4. [Technology and Requirements](#technology-and-requirements)
5. [Installation and Running](#installation-and-running)
6. [Feature Walkthrough](#feature-walkthrough)
7. [Architecture](#architecture)
8. [Project Structure](#project-structure)
9. [Database and Data Access](#database-and-data-access)
10. [Java Concepts and Design](#java-concepts-and-design)
11. [Security](#security)
12. [Testing and Verification](#testing-and-verification)
13. [Current Limitations](#current-limitations)
14. [Possible Improvements](#possible-improvements)

---

## Overview

HealthConnect is a role-based web application for managing common workflows involved in online health consultations.

It provides separate workspaces for:

- **Patients**, who can book consultations, view advice, manage health records and profile details, and message doctors.
- **Doctors**, who can manage weekly availability, review consultations, provide advice and prescriptions, access relevant patient records, and respond to messages.
- **Administrators**, who can manage users and roles, manage appointments, configure system settings, and view dashboard analytics.

The application is built using Java Servlets and JSP, with JDBC for database access and MySQL for persistence. The interface uses HTML, CSS, JavaScript, Font Awesome icons, and Chart.js for the admin analytics charts.

## Features

### Patient Portal

- Patient self-registration and login.
- Patient dashboard and role-specific navigation.
- View available doctors and book consultations.
- Appointment availability uses doctor schedules and free-slot calculation.
- View consultation advice and prescription information provided by doctors.
- Create, view, update, and delete personal health records.
- Update profile information and password.
- Send messages to doctors and view replies.
- Access patient-specific sections for booking, advice, records, messages, and profile.

### Doctor Portal

- Doctor login and doctor-specific dashboard.
- Manage weekly availability and schedules.
- View and manage consultations associated with the doctor.
- Record consultation advice and prescription details.
- Access patient records for patients who have appointments with that doctor.
- Send and receive messages through the communication section.

### Administrator Portal

- View dashboard statistics and analytics charts.
- Manage user accounts, including role and activation status.
- Create and manage doctor accounts. Doctor specialization is required.
- Review, schedule/manage, and delete appointments through the admin workflow.
- Configure platform/system settings.
- View user and appointment summaries, including charts for user roles and appointment statuses.

### Messaging

- Patient-to-doctor messaging.
- Doctors can reply to patient messages.
- Replies can be viewed by the patient in the messaging section.

### Interface

- Separate patient, doctor, and administrator dashboards.
- Sidebar navigation with Font Awesome icons.
- Styled forms, tables, status badges, dashboard cards, and buttons.
- Hover transitions and interface animations.
- Responsive CSS rules for smaller screens.
- Admin analytics charts rendered with Chart.js.

### Appointment Payment Interface — Current Scope

The current application includes a payment interface/action in the appointment workflow. At this stage, a real payment gateway has **not** been integrated. The interface should not be described as processing real online payments.

---

## Problems Addressed

| Problem | How HealthConnect addresses it |
|---|---|
| Patients need a central place to request consultations | Doctor selection and appointment booking |
| Doctor availability can be difficult to coordinate | Weekly schedule and free-slot calculation |
| Consultation advice can be hard to keep organized | Consultation advice and prescription details |
| Personal health information can be scattered | Patient health-record management |
| Patients and doctors need a way to follow up | Built-in patient-doctor messaging |
| Administrators need to manage platform users | User management, roles, and activation controls |
| Platform activity needs an overview | Admin statistics and analytics charts |

## Technology and Requirements

| Area | Technology |
|---|---|
| Language | Java |
| Web request handling | Java Servlets |
| Views | JSP and JSTL |
| Database access | JDBC |
| Database | MySQL 8 |
| Build and packaging | Apache Maven |
| Application server | Apache Tomcat 9 |
| Frontend | HTML, CSS, JavaScript |
| Icons | Font Awesome |
| Admin charts | Chart.js |
| Version control | Git and GitHub |

### Required software

- JDK 11 or later, compatible with the Maven configuration.
- Apache Maven.
- MySQL 8.
- Apache Tomcat 9.
- A modern web browser.

**Compatibility note:** The project uses the `javax.servlet` API and is configured for Tomcat 9. Tomcat 10 uses the `jakarta.servlet` namespace and is not a drop-in replacement.

---

## Installation and Running

### 1. Get the source code

```bash
git clone https://github.com/KrishnaRajKRcodes/HealthConsultation.git
cd HealthConsultation
```

You can also download the repository as a ZIP from GitHub.

### 2. Create the database

Start MySQL and run the schema from the project root:

```bash
mysql -u root -p < sql/schema.sql
```

Alternatively, open `sql/schema.sql` in MySQL Workbench and execute it.

**Important:** Review the SQL script before running it against a database that contains data you need to keep.

### 3. Configure the database connection

The database configuration file is local and is excluded from the repository because it contains credentials.

Create this file on your computer:

`src/main/resources/db.properties`

Use your own local MySQL credentials:

```properties
driver=com.mysql.cj.jdbc.Driver
url=jdbc:mysql://localhost:3306/health_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
username=YOUR_MYSQL_USERNAME
password=YOUR_MYSQL_PASSWORD
```

Replace the placeholder values with your actual local configuration. Do not commit this file or publish its password.

### 4. Build the application

From the project root, run:

```bash
mvn clean package
```

The WAR package should be generated at:

```text
target/health-platform.war
```

### 5. Deploy to Tomcat 9

Copy `target/health-platform.war` into Tomcat's `webapps` directory. For example:

```text
<TOMCAT_HOME>/
└── webapps/
    └── health-platform.war
```

Start or restart Tomcat and wait for the application to deploy.

### 6. Open the application

Visit:

```text
http://localhost:8080/health-platform/
```

### 7. Initial account setup

On first startup, the application initializes default system settings and an administrator account. The current code's default demonstration account is:

- **Email:** `admin@health.com`
- **Password:** `Admin@123`

These are development/demo credentials. Change the password immediately after the first login and do not use default credentials in a public deployment. Patients can register themselves; administrators create doctor accounts, and a doctor's specialization is required.

If login does not work, check that MySQL is running, the local `db.properties` values are correct, the schema has been created, and Tomcat has deployed the WAR without errors.

---

## Feature Walkthrough

### 1. Patient registration and login

A patient registers through the application and signs in to access the patient dashboard.

### 2. Booking a consultation

The patient selects a doctor and uses the appointment workflow to choose an available slot. Availability is calculated from the doctor's schedule and existing bookings.

### 3. Doctor schedule management

A doctor uses the schedule section to manage weekly availability. This schedule is used by the appointment workflow.

### 4. Consultation advice and prescriptions

Doctors can review consultations and record advice and prescription details. Patients can view the information made available to them.

### 5. Health records

Patients can create, view, update, and delete their health-record entries. Doctors' access to patient records is limited to patients who have appointments with them.

### 6. Messaging

Patients can send messages to doctors. Doctors can reply, and patients can view those replies in their messages section.

### 7. Administration and analytics

Administrators can manage users, roles, account activation, appointments, and platform settings. The dashboard displays user and appointment statistics, including charts for user roles and appointment statuses.

---

## Architecture

The project separates request handling, business logic, data access, and presentation.

```text
Browser
   |
   v
JSP / JSTL / HTML / CSS / JavaScript
   |
   v
Servlets
(AuthServlet, PatientServlet, DoctorServlet, AdminServlet)
   |
   v
Service Layer
(UserService, AppointmentService, MessageService, ScheduleService)
   |
   v
DAO Layer
(UserDAO, AppointmentDAO, ConsultationDAO, ScheduleDAO, ...)
   |
   v
JDBC
   |
   v
MySQL
```

### Model layer

The model package contains application entities and role-related types, including `User`, `Patient`, `Doctor`, `Admin`, `Appointment`, `Consultation`, `HealthRecord`, `Message`, and `Schedule`.

### DAO layer

The Data Access Object (DAO) layer contains SQL/database operations and separates persistence code from servlet request handling. It includes DAOs for users, appointments, consultations, health records, messages, schedules, and settings.

### Service layer

Service classes hold application operations and business rules used by the servlets, including user, appointment, message, and schedule services.

### Servlet layer

Servlets receive HTTP requests, coordinate the relevant operations, and select the view or response. Main servlet classes include `AuthServlet`, `PatientServlet`, `DoctorServlet`, and `AdminServlet`.

### JSP presentation layer

JSP pages render the login, registration, patient, doctor, administrator, and error views. Views are kept under `WEB-INF/views/`.

### Filters and initialization

- `AuthFilter` applies authentication and role-based access checks.
- `CsrfFilter` validates CSRF tokens on POST requests.
- `AppInitListener` initializes application settings and the default administrator account.

---

## Project Structure

```text
HealthConsultation/
├── README.md
├── .gitignore
├── pom.xml
├── sql/
│   └── schema.sql
└── src/
    └── main/
        ├── java/
        │   └── com/healthcare/
        │       ├── dao/
        │       │   ├── AppointmentDAO.java
        │       │   ├── ConsultationDAO.java
        │       │   ├── CrudDAO.java
        │       │   ├── HealthRecordDAO.java
        │       │   ├── MessageDAO.java
        │       │   ├── ScheduleDAO.java
        │       │   ├── SettingsDAO.java
        │       │   └── UserDAO.java
        │       ├── filter/
        │       │   ├── AuthFilter.java
        │       │   └── CsrfFilter.java
        │       ├── listener/
        │       │   └── AppInitListener.java
        │       ├── model/
        │       ├── service/
        │       ├── servlet/
        │       └── util/
        ├── resources/
        │   └── db.properties  (local only; ignored by Git)
        └── webapp/
            ├── index.jsp
            ├── css/
            │   └── style.css
            └── WEB-INF/
                ├── web.xml
                └── views/
                    ├── admin.jsp
                    ├── doctor.jsp
                    ├── patient.jsp
                    ├── login.jsp
                    ├── register.jsp
                    ├── error.jsp
                    └── fragments/
```

The repository contains additional model, service, servlet, utility, and JSP fragment files beyond the abbreviated tree shown above.

---

## Database and Data Access

The SQL schema is stored in `sql/schema.sql`. The application connects to MySQL through JDBC, with connection settings read from the local `db.properties` configuration.

The persistence layer includes DAO classes for:

- Users and roles.
- Appointments.
- Consultations.
- Health records.
- Messages.
- Doctor schedules.
- System settings.

The schema and DAO implementations are the source of truth for exact table definitions, keys, relationships, and constraints.

## Java Concepts and Design

The current codebase demonstrates several Java application design concepts:

- **Object-oriented design:** domain objects represent users, roles, appointments, consultations, records, messages, and schedules.
- **Inheritance and role-specific models:** `User` is extended by `Admin`, `Doctor`, and `Patient`.
- **Enums:** role and appointment status values are represented with enum types.
- **Factory pattern:** `UserFactory` supports creation of role-specific user objects.
- **DAO pattern:** database operations are separated from the service and web layers.
- **Service layer:** business operations are organized in dedicated service classes.
- **Exception handling:** application-specific exceptions include `DAOException` and `ValidationException`.
- **Input validation:** validation helpers centralize checks used by the application.

## Security

The project includes authentication and role checks through `AuthFilter`, CSRF token validation through `CsrfFilter`, password-related utilities, and validation helpers.

These controls do not by themselves establish that the application is production-ready. Before public deployment:

- Change the default administrator password.
- Use a strong, private MySQL password.
- Keep `db.properties` and other credentials out of Git.
- Use HTTPS and review session-cookie settings.
- Use a database account with only the permissions the application needs.
- Review authorization and validation for all sensitive operations.
- Do not use real patient information in public demos, screenshots, or test data.

If a credential has already been exposed, rotate it rather than relying only on `.gitignore`.

## Testing and Verification

The following are practical manual checks for the implemented workflows:

| Area | Manual check |
|---|---|
| Authentication | Register a patient and sign in |
| Role access | Verify patient, doctor, and admin accounts reach their intended sections |
| Appointment booking | Book a slot that is available |
| Scheduling | Change a doctor's weekly availability and check the booking flow |
| Consultation | Record consultation advice/prescription and verify the patient can view it |
| Health records | Create, update, view, and delete a patient record |
| Messaging | Send a patient message, reply as the doctor, and check the patient view |
| Admin users | Change user roles or activation status using admin controls |
| Admin appointments | Review and manage appointments |
| Analytics | Check that dashboard counts and charts render |
| CSRF/auth filters | Check that protected requests and role-restricted pages enforce their checks |

These are manual verification scenarios; this README does not claim that an automated test suite exists.

## Current Limitations

- A real payment gateway is not integrated; the current payment interface is only part of the appointment workflow.
- The application requires local MySQL configuration and a compatible Tomcat deployment.
- Default administrator credentials must be changed before any shared or public deployment.
- The project has not been represented here as a production-certified healthcare system.
- No automated test suite is claimed in this documentation.

## Possible Improvements

These are potential future enhancements, not claims about the current implementation:

- Integrate a real payment gateway and verify payment status before confirming paid appointments.
- Add appointment email/SMS reminders.
- Add automated unit and integration tests.
- Add deployment configuration and environment-based secret management.
- Add documented screenshots and a database ER diagram.
- Improve audit logging, monitoring, accessibility, and production security review.
- Add deployment instructions for a hosted environment.

---

## Repository

Source code: https://github.com/KrishnaRajKRcodes/HealthConsultation

## License

No open-source license is currently specified in this README. Add a license file if you intend to define permissions for reuse and distribution.
