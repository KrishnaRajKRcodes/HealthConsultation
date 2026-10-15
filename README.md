# Online Health Consultation Platform (Java Servlets + JSP + JDBC + MySQL)

## Run it
1. Install JDK 11+, Maven, MySQL 8 and Apache Tomcat 9 (javax.servlet; not Tomcat 10).
2. Create the schema:  `mysql -u root -p < sql/schema.sql`
3. Edit `src/main/resources/db.properties` (MySQL user/password).
4. Build: `mvn clean package`  ->  copy `target/health-platform.war` into Tomcat's `webapps/`.
5. Open http://localhost:8080/health-platform/

First start auto-creates default settings and the admin account:
**admin@health.com / Admin@123** (change it after first login).
Patients self-register; admins create doctors (Specialization is required for doctors).

## Architecture
model (User -> Admin/Doctor/Patient, enums, UserFactory) -> dao (JDBC, CrudDAO<T>) -> service (business rules)
-> servlet (AuthServlet, AdminServlet, DoctorServlet, PatientServlet) -> JSP/JSTL views in WEB-INF/views.
Filters: AuthFilter (role-based access), CsrfFilter (token on every POST). Listener: AppInitListener (seeding).

## Feature map
Admin   : user CRUD + roles/activation, schedule/review/delete appointments, system settings, analytics.
Doctor  : weekly availability, consultations (advice + prescription, transactional), patient records
          (only patients they have appointments with), messaging.
Patient : book consultations (free-slot calculation), view advice, CRUD health records, profile + password, messaging.
