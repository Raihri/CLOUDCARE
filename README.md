# CloudCare — 8-Mark Project Report

A short, structured report describing the CloudCare project, its purpose, design, and next steps. This version is meant to satisfy an 8-mark assignment: clear headings, concise explanations, and a complete overview.

---

## 1. Introduction

### 1.1 About the Project
CloudCare is a Spring Boot + Thymeleaf web application for managing doctors, patients, blood donors, and blood requests. It aims to simplify donor discovery, appointment handling, and basic patient record management while providing simple admin dashboards and lightweight AI-assisted help.

### 1.2 Motivation
The project was created to provide a compact platform that helps connect patients in need of blood with eligible donors, while offering basic clinic management features (doctor signup, appointments, patient records). It also serves as a learning project for full-stack Java web development using Spring Boot and Thymeleaf.

### 1.3 Features (bulleted with short descriptions)
- **Donor registration & eligibility checks:** collects donor data and evaluates eligibility based on age, blood group, disease history, and last donation date.
- **Blood request creation & matching:** patients or admins create requests; donors can be filtered and matched by blood group and location.
- **Doctor management & appointments:** doctors sign up, add availability, and patients can book appointments.
- **Patient records:** basic vitals, lab values, and history fields to store patient information.
- **Email notifications:** registration confirmations, eligibility notices and request updates via email (Spring Mail).
- **Image uploads & PDFs:** Cloudinary-backed image uploads and PDF generation for reports (Flying Saucer).
- **AI support (optional):** simple chat feature integrating OpenAI for guidance or FAQs.

### 1.4 Tools, Technologies and Frameworks
- **Backend:** Java 17, Spring Boot (Web, Data JPA, Security, Mail). 
- **Persistence:** PostgreSQL, Spring Data JPA, Hibernate. 
- **Frontend / Templates:** Thymeleaf, static JS/CSS under `src/main/resources/static`.
- **Other libraries/services:** Cloudinary (images), Flying Saucer (PDFs), OpenAI for AI features.
- **Build & Dev:** Maven (`pom.xml`), Lombok for model code reduction.

---

## 2. Design and Implementation

### 2.1 High-level Architecture
- **Layered design:** controllers → services → repositories (Spring Data JPA). Views are server-rendered with Thymeleaf; specific pages use client-side JS for interactive dashboards.
- **Key services:** DonorService (eligibility and saving donors), RequestService (create/filter requests), EmailService (async mail), and Doctor/Patient services for domain logic.
- **Authentication & roles:** basic role checks exist (admin/doctor/patient), with scope for enhancement (e.g., role-based access control and stronger session management).

### 2.2 User Flows (concise)
- **Doctor signup flow:** doctor fills form → controller validates → saved as pending → admin approves or rejects.
- **Donor registration flow:** donor submits form → DonorService checks eligibility → donor saved and emailed confirmation with status.
- **Request/Match flow:** patient creates request → system filters donors by blood group and location → admin or patient contacts donors.

### 2.3 Database Schema (key tables & relationships)
- **Donor**: id, name, age, gender, email, contact, bloodGroup, lastDonated, diseases (ElementCollection), status, location, createdAt
- **Request**: id, name, email, bloodGroup, units, requiredDate, status, medicalReason, createdAt
- **Doctor**: id, name, email (unique), bmdcRegNo (unique), degrees, specialization, availability (OneToMany), status
- **Patient**: id (maps to User), bloodGroup, vitals, allergies, diseases, notifications (OneToMany)
- **Appointment**: doctor (ManyToOne), patient (ManyToOne), datetime, status

Foreign key constraints use JPA annotations; some lists are stored using ElementCollection for simplicity.

### 2.4 File Structure (important paths)
- `src/main/java/com/example/cloud/care/controller` — HTTP controllers (examples: `doctor_controller.java`)
- `src/main/java/com/example/cloud/care/service` — business logic (`DonorService`, `RequestService`, `EmailServiceDonor`)
- `src/main/java/com/example/cloud/care/model` — JPA entities (`Donor.java`, `Doctor.java`, `Patient.java`, etc.)
- `src/main/resources/templates` — Thymeleaf HTML templates
- `src/main/resources/static` — CSS, JS, images
- `src/main/resources/application.properties` — configuration (DB, mail, Cloudinary, API keys)

> Security note: `application.properties` contains API keys and passwords in this repo; move these to environment variables or a `.env` file in real deployments.

---

## 3. Conclusion

### 3.1 Challenges and How They Were Addressed
- **Complex eligibility rules for donors:** implemented centralized eligibility checks in `DonorService` with clear rules and human-readable reasons returned.
- **Managing uploaded images and privacy:** used Cloudinary and set multipart upload limits, also added optional public/private restrictions on images.
- **Reliable email delivery and async tasks:** used Spring Mail with async wrappers and a background email queue to avoid blocking requests.

### 3.2 Lessons Learned
- Separate domain rules from controllers for testability and clarity.
- Keep configuration and secrets out of version control; use env vars and `.env.example` alongside documentation.
- Prefer small, readable JPA entities and ElementCollection for simple lists to reduce schema complexity.

### 3.3 Future Work
- Improve UI and UX (responsive design, accessible forms, consistent theme).
- Strengthen security (role-based access control, CSRF hardening, password policies).
- Add location-based donor search, SMS notifications, and improved matching algorithms.
- Add unit and integration tests (increase coverage) and CI/CD pipeline for automated builds and deployments.

---

## Quick Setup (run locally)
1. Install Java 17 and Maven.
2. Set environment variables for sensitive values (recommended):
   - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
   - `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`
   - `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`
   - `OPENAI_API_KEY` (optional)
3. Run:

```bash
mvn spring-boot:run
```
4. Open `http://localhost:8080` in a browser.

---

*This report is written to match an 8-mark requirement: it covers the project summary, motivation, features, tools, design, schema, challenges and future plans in a concise, exam-style format.*

