# CloudCare Frontend

Next.js (App Router) frontend for CloudCare. Replaces the legacy Thymeleaf
templates with one consistent design system.

## Running locally

The frontend talks to the Spring Boot backend over a **same-origin reverse
proxy** — the browser only ever talks to `:3000`, so CORS is never involved and
the `JSESSIONID` session cookie stays first-party.

1. Start Spring Boot on `:8080` (from the repo root: `./run-backend.sh`).
2. Optional: copy `.env.example` to `.env.local` only if the backend is not on
   `http://localhost:8080`.
3. Start Next:

   ```bash
   npm install
   npm run dev
   ```

   → http://localhost:3000

## Design system

`src/app/globals.css` is the single source of truth for color, type, radius and
elevation.

**Rule: never type a raw hex value in a component.** If a color is needed, add
it to the token layer first. This is what keeps the palette from fragmenting
again — the legacy frontend had ~370 distinct hex values and 52 competing blues.

`--color-blood` is deliberately restricted to blood group chips, urgency badges
and critical alerts. It is not a general-purpose red.

## Layout

```
src/
  app/
    (auth)/            /login, /register, /forgot-password  (route group: no URL segment)
    (patient)/         patient portal: /dashboard, /appointments, /doctors, ...
    doctor/            doctor portal (/doctor/*)
    admin/             admin portal (/admin/*)
    api/logout/        POST logout relay to Spring
  components/
    layout/            AppShell, role-aware sidebar + nav definitions
    ui/                Button, Card, Form, Badge, Table, Feedback, PageHeader
    auth/ brand/ marketing/ notifications/ patient/ prescriptions/
  lib/
    api.ts             server-side fetch wrapper (forwards JSESSIONID), SPRING_API_URL
    auth.ts            getCurrentUser / requireUser / requireRole
    *-api.ts           typed clients per role
```

## Auth model

Spring Security owns authentication. Next.js never stores credentials — it
forwards `JSESSIONID` on every server-side request and asks `GET /api/me` who is
signed in. `requireUser()` / `requireRole()` guard pages; `AppShell` renders the
nav for the active role.