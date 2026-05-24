# MyAdvisor - Student Academic Advising & Booking Platform

[![Build Status](https://github.com/Schramm2/CS-Capstone-Project/actions/workflows/maven.yml/badge.svg)](https://github.com/Schramm2/CS-Capstone-Project/actions/workflows/maven.yml)

**MyAdvisor** is a role-based academic advising and appointment platform built with **Java**, **Spring Boot**, **Vaadin Flow**, **JPA**, and **MySQL**. It streamlines communication, file sharing, graduation planning, and meeting bookings between students, academic advisors, and faculty administrators.

> [!NOTE]
> This repository was created as a final-year Computer Science Capstone Project at the University of Cape Town (UCT) by Matthew Schramm, Josh Birkholtz, and Connor Thompson.

---

## 🚀 Product Overview

Navigating university degree requirements, booking slots with busy advisors, and keeping track of required vs. elective credits can be fragmented and confusing. MyAdvisor serves as a centralized hub that brings students and advisors together under one system. 

It features real-time chat, transcript/document upload, automated course tracking ("Smart Tutor"), and automated slot booking.

### Core Features
- **Real-Time Reactive Chat:** Direct messaging between students and their major advisors, and collaboration channels for faculty advisors.
- **Smart Tutor (Graduation Track Planner):** Interactive course grids that calculate accumulated credits, mark required/elective courses, and track graduation readiness.
- **Academic File Sharing:** Secure upload (PDF/PNG) of transcripts, prerequisite waivers, and advising notes.
- **Appointment Scheduling:** Automated slot management where advisors publish availability and students book slots.
- **Granular Role-Based Access Control:** Configured security policies protecting administrative, advisor, and student views.

---

## 👥 Capstone Context & Team Role

- **Team Members:** Matthew Schramm, Josh Birkholtz, Connor Thompson
- **Context:** University of Cape Town (UCT) CS Capstone Project
- **My Contributions (Matthew Schramm):**
  - **Full-Stack Feature Engineering:** Designed and built the *Smart Tutor* interactive course grids and degree progress calculation engine.
  - **File Sharing Pipeline:** Implemented the file upload, download, and ownership-based deletion system using Vaadin's custom receivers and disk storage.
  - **UI/UX Design & Data Binding:** Developed and styled responsive dashboards using Vaadin Flow's component model and binders to validate form fields.
  - **Security Integration:** Integrated role-based route protection using Spring Security and Vaadin's navigation decorators.

---

## 🔑 Role-Based Access Model

The system enforces strict permission boundaries across five distinct user roles:

| Role | Permissions & Capabilities |
| :--- | :--- |
| **Admin** | System-wide configuration. Can create, edit, and delete Student, Advisor, Faculty, Department, and Semester records. |
| **Faculty Advisor** | Administrative oversight of a specific Faculty. Can manage Department and Degree mappings, as well as advisor assignments. |
| **Senior Advisor** | Department-level administration. Can manage majors, advisors, and publish/update course catalogs. |
| **Advisor** | Direct student interaction. Can publish meeting slots, approve booking requests, view/upload student academic files, access students' *Smart Tutor* trackers, and chat with assigned majors. |
| **Student** | Personal portal. Can book advisor slots, chat with major advisors, upload transcripts, and view/interactively plan courses on *Smart Tutor*. |

---

## 🛠️ Tech Stack & Architecture

- **Backend:** Java 17, Spring Boot 3.2.x, Spring Security
- **Frontend:** Vaadin Flow 24.x (Server-Side Web App model with real-time UI synchronization)
- **Data Access:** Spring Data JPA, Hibernate
- **Database:** MySQL 8.x
- **SSH Tunneling:** JSch (for secure remote DB communication)
- **Build Tool:** Maven (packaged with Maven Wrapper)

---

## 📦 Local Setup Instructions

### Prerequisites
- **Java JDK 17** or higher installed.
- **MySQL 8.x** server running locally or accessible remotely.

### 1. Database Configuration
Create a database named `MyAdvisor` in your MySQL instance:
```sql
CREATE DATABASE MyAdvisor;
```

### 2. Environment Variables
The application reads database configuration from environment variables. Set the following variables in your local shell or IDE environment:

```bash
# Database Settings
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/MyAdvisor
export SPRING_DATASOURCE_USERNAME=your_mysql_user
export SPRING_DATASOURCE_PASSWORD=your_mysql_password

# Optional: SSH Tunnel settings (disabled by default)
export SSH_TUNNEL_ENABLED=false
```

> [!IMPORTANT]
> **Security Notice:** Past commits in this repository's git history contain legacy credentials. These credentials have been fully rotated and are invalid. Any active production deployments should configure credential injections strictly via secure environment variables.

### 3. Run the Application
From the repository root, change directory to the maven project and use the Maven Wrapper:

```bash
cd myadvisor
./mvnw clean spring-boot:run
```

Once started, open [http://localhost:8080](http://localhost:8080) in your web browser.

---

## 👥 Demo Users

The application automatically seeds a set of dummy test accounts when starting up on an empty database (configured via [data.sql](myadvisor/src/main/resources/data.sql)). All passwords default to `admin` for testing convenience:

* **Science Faculty:**
  * **Faculty Admin:** `facultysci`
  * **Senior Advisor & Advisor:** `senioradvisorsci`
  * **Advisor:** `advisorsci`
  * **Student (BSc CS):** `studentsci`
* **Commerce Faculty:**
  * **Faculty Admin:** `facultycom`
  * **Senior Advisor & Advisor:** `senioradvisorcom`
  * **Advisor:** `advisorcom`
  * **Student (BCom IS):** `studentcom`

---

## 📸 Screenshots

*Insert screenshots here to demonstrate the application visual style:*

| Smart Tutor Tracker | Meeting Booking Dashboard | Real-time Advisor Chat |
| :---: | :---: | :---: |
| ![Smart Tutor Placeholder](https://via.placeholder.com/400x250?text=Smart+Tutor+Grid) | ![Booking Dashboard Placeholder](https://via.placeholder.com/400x250?text=Meeting+Booking+Grid) | ![Chat Interface Placeholder](https://via.placeholder.com/400x250?text=Vaadin+Chat+View) |

*Instructions: Save actual PNG screenshots in `myadvisor/src/main/resources/META-INF/resources/images/` and update these image links to absolute or relative markdown references.*

---

## 🏗️ Production Packaging

To compile frontend resources and bundle the application into a single standalone production-ready JAR file, run:

```bash
cd myadvisor
./mvnw clean package -Pproduction
```
The compiled JAR can be executed directly:
```bash
java -jar target/my-advisor-1.0-SNAPSHOT.jar
```

---

## 📝 Known Limitations

- **Stateful Server-Side Architecture:** Vaadin Flow stores UI component state in the HTTP session. This makes frontend development simple but limits horizontal scaling without session replication.
- **Local Disk File Storage:** Uploaded files are saved to the server's local build directory (`target/files/`). In a production setting, this should be refactored to use a persistent external volume or cloud storage (e.g., AWS S3).
- **Test Coverage:** The project was built under a fast academic capstone timeline and currently lacks automated unit or integration tests.

---

## 💡 Retrospective: "What I Would Improve Today"

If I were to rebuild or refactor this system today, I would focus on the following enhancements:
1. **Cloud File Storage:** Replace local disk file writes with an abstract storage client (like Spring Cloud AWS) to push documents directly to S3.
2. **Database Performance Indexing:** Optimize JpaRepositories by indexing queries on highly referenced columns such as `student_id` in `SmartTutorCourse` and matching messages sender/receiver keys.
3. **Containerization:** Create a multi-stage Docker build separating the Java builder from a minimal runtime image, and configure docker-compose for easy local spin-ups of both Spring Boot and MySQL.
4. **CI/CD Test Automation:** Add JUnit 5 unit tests for core services and integration tests using Spring Boot's `@SpringBootTest` alongside H2 database profiles.

---

## 📄 License

This project is licensed under the Unlicense - see the [LICENSE](LICENSE) file for details.

---

## 🏷️ Suggested GitHub Repository Metadata

If you are setting up this repository on GitHub, here are the recommended metadata settings:

- **Description:** `Role-based academic advising and appointment platform built with Spring Boot, Vaadin, JPA, and MySQL.`
- **Topics:** `spring-boot`, `vaadin`, `java`, `jpa`, `mysql`, `maven`, `student-services`, `capstone-project`
