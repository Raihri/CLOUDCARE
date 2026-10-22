import "server-only";

import { springJson, unwrap } from "@/lib/api";

/**
 * Types for the doctor JSON API (DoctorApiController).
 *
 * Generated from that controller's DTO builders. As with the patient API,
 * nothing here is validated against the wire at compile time — if you change a
 * DTO builder, change the matching type.
 */

export type DoctorProfile = {
  id: number;
  name: string | null;
  email: string | null;
  profileImage: string | null;
  bloodGroup: string | null;
  gender: string | null;
  phoneNumber: string | null;
  altPhone: string | null;
  address: string | null;
  division: string | null;
  zilla: string | null;
  bmdcRegNo: string | null;
  degrees: string | null;
  specialization: string | null;
  education: string | null;
  medicalCollege: string | null;
  experienceYears: number | null;
  certifications: string | null;
  languages: string | null;
  hospitalName: string | null;
  hospitalAddress: string | null;
  consultationFee: string | null;
  description: string | null;
  workingDays: string | null;
  workingHours: string | null;
  specialInterests: string | null;
  rating: string | null;
  telemedicineAvailable: boolean | null;
  status: string | null;
};

export type DoctorSlot = {
  id: number;
  date: string | null;
  startTime: string | null;
  endTime: string | null;
  booked: boolean;
  telemedicineAvailable: boolean | null;
};

export type DoctorAppointment = {
  id: number;
  patientId: number | null;
  patientName: string | null;
  appointmentDate: string | null;
  timeSlot: string | null;
  type: string | null;
  status: string | null;
  telemedicineLink: string | null;
};

export type DoctorDashboard = {
  success: boolean;
  name: string | null;
  status: string | null;
  totalAppointments: number;
  pendingAppointments: number;
  confirmedAppointments: number;
  cancelledAppointments: number;
  openSlots: number;
  upcoming: DoctorAppointment[];
};

export type DoctorPatient = {
  id: number;
  name: string | null;
  email: string | null;
  bloodGroup: string | null;
  gender: string | null;
  age: number | null;
  zilla: string | null;
  appointments: number;
  lastVisit: string | null;
};

// Each degrades to an empty result so one failing panel doesn't 500 the page.
// The read used for a whole page (fetchDoctorProfile) returns null instead, so
// the caller can distinguish "no profile" from "loaded an empty one".

export async function fetchDoctorProfile(): Promise<DoctorProfile | null> {
  try {
    const body = await springJson<{ success: boolean } & DoctorProfile>(
      "/doctor/api/me",
    );
    return body?.success ? body : null;
  } catch {
    return null;
  }
}

export async function fetchDoctorDashboard(): Promise<DoctorDashboard | null> {
  try {
    return await springJson<DoctorDashboard>("/doctor/api/dashboard");
  } catch {
    return null;
  }
}

export async function fetchDoctorSlots(): Promise<DoctorSlot[]> {
  try {
    return unwrap<DoctorSlot>(await springJson("/doctor/api/availability"));
  } catch {
    return [];
  }
}

export async function fetchDoctorAppointments(): Promise<DoctorAppointment[]> {
  try {
    return unwrap<DoctorAppointment>(await springJson("/doctor/api/appointments"));
  } catch {
    return [];
  }
}

export async function fetchDoctorPatients(): Promise<DoctorPatient[]> {
  try {
    return unwrap<DoctorPatient>(await springJson("/doctor/api/patients"));
  } catch {
    return [];
  }
}