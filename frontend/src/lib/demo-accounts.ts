/**
 * Credentials for the one-click demo buttons.
 *
 * These match the patient/doctor accounts seeded by `DemoDataSeeder` and the
 * admin bootstrapped by `AdminBootstrapConfig`. Keep the files in sync if the
 * demo data changes.
 */
export type DemoRole = "PATIENT" | "DOCTOR" | "ADMIN";

export type DemoAccount = {
  role: DemoRole;
  label: string;
  name: string;
  email: string;
  password: string;
  detail: string;
  icon: string;
};

export const DEMO_ACCOUNTS: DemoAccount[] = [
  {
    role: "PATIENT",
    label: "Patient",
    name: "Ayesha Siddiqua",
    email: "demo.patient@cloudcare.test",
    password: "Demo@123",
    detail: "Appointments, records, alerts",
    icon: "bxs-user",
  },
  {
    role: "DOCTOR",
    label: "Doctor",
    name: "Dr. Nusrat Jahan",
    email: "demo.doctor@cloudcare.test",
    password: "Demo@123",
    detail: "Patients, slots, telemedicine",
    icon: "bxs-first-aid",
  },
  {
    role: "ADMIN",
    label: "Admin",
    name: "Administrator",
    email: "demo.admin@cloudcare.test",
    password: "admin123",
    detail: "Approvals, donors, feedback",
    icon: "bxs-shield",
  },
];
