# CLOUDCARE

**CloudCare** is a Spring Boot (Java 17) web application that provides healthcare-related features for doctors, patients, and admins. It enables user management, file uploads, PDF generation, email notifications, all backed by PostgreSQL
---

## Quick Links

- **Project:** `cloud-care` (v0.0.1-SNAPSHOT)
- **Port:** 8080 (default)
---

## Live Demo
- **Main site:** https://cloudcare-3.onrender.com/￼
- **Admin panel:** https://cloudcare-3.onrender.com/admin/login￼

Note: Admin has a separate login page.
---

## Features

- Role-based authentication:
	•	Patient: Upload reports, view prescriptions
	•	Doctor: Manage patient files, generate PDFs, send notifications
	•	Admin: Manage users, monitor system
- Spring Boot 3.x with Thymeleaf templates
- Spring Security for authentication/authorization
- File uploads using Cloudinary (images, raw files)
- PDF generation (Flying Saucer)
- Email notifications (Brevo / SMTP)
- PostgreSQL via Spring Data JPA


---

## Requirements

- Java 17 (JDK)
- Maven (or use the included `./mvnw` wrapper)
- PostgreSQL (or equivalent DB compatible with JDBC)
- Docker (optional)

---

## Local Setup & Run

1. Clone the repo:

```bash
git clone https://github.com/Raihri/CLOUDCARE.git
cd CLOUDCARE
```

2. Provide environment variables (see `application.properties`) — recommended: create a `.env` or set them in your shell. Minimal example:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cloudcare
export SPRING_DATASOURCE_USERNAME=cloudcare
export SPRING_DATASOURCE_PASSWORD=secret
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=adminpass
export CLOUDINARY_CLOUD_NAME=...
export CLOUDINARY_API_KEY=...
export CLOUDINARY_API_SECRET=...
export OPENAI_API_KEY=...
export BREVO_API_KEY=...
```

3. Build and run:

```bash
./mvnw clean package -DskipTests
java -jar target/cloud-care-0.0.1-SNAPSHOT.jar
# or in dev
./mvnw spring-boot:run
```

4. Access app:

	•	Main: http://localhost:8080￼
	•	Admin: http://localhost:8080/admin/login￼


---

##  Docker

Build the image:
```bash
docker build -t cloud-care:latest .
```

Run with env file:

```bash
docker run -p 8080:8080 --env-file .env cloud-care:latest
```

---

##  Important Environment Variables

- `SPRING_APPLICATION_NAME`
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATASOURCE_DRIVER_CLASS_NAME`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`, `SPRING_JPA_SHOW_SQL`, `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT`
- `SPRING_THYMELEAF_CACHE`
- `BREVO_API_KEY`, `BREVO_SENDER_EMAIL`, `BREVO_SENDER_NAME`
- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`
- `OPENAI_API_KEY`
- `ADMIN_USERNAME`, `ADMIN_PASSWORD`

> Tip: For local development keep secrets in a `.env` file that is added to `.gitignore`.

---

##  Troubleshooting

- App fails to connect to DB: verify `SPRING_DATASOURCE_URL` and DB is running.
- Broken uploads: check Cloudinary env vars and folder permissions.
- Password reset links: ensure emails are delivered by configuring Brevo/SMTP correctly.



## Contributing

1.	Fork the repo
2.	Create a new branch: git checkout -b feature/your-feature
3.	Make changes and commit: git commit -m "Add your feature"
4.	Push branch and create a pull request
