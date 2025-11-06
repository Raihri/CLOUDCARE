# Doctors Directory — Spring Boot app (CRUD, editable ID)

This is a Spring Boot web application demonstrating a doctors directory with full CRUD:
- List all doctors
- View details
- Create new doctor
- Edit any field including ID (if ID is changed, old profile is removed and new one saved — if another profile used that ID it will be replaced)
- Delete doctor

Data is stored in-memory (ConcurrentHashMap) so it is lost when the app stops. Intended for demo / local use. You can replace the repository with a DB-backed implementation later.

## Run

Requirements:
- Java 17+
- Maven

From the project root:
```bash
mvn spring-boot:run
```

Open http://localhost:8080/doctors

Notes:
- The form accepts all fields listed in your specification.
- When editing an existing doctor there is a hidden `oldId` parameter; if you change the ID value before saving, the app will remove the old entry and save under the new ID. If the new ID was already present it will be overwritten (per your requirement to replace instead of creating duplicates).
