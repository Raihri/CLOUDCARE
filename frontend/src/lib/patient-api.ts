import "server-only";

import { springJson, unwrap } from "@/lib/api";

/**
 * Types for the patient JSON API.
 *
 * These were generated from the DTOs in PatientApiController and verified
 * against live responses, so field names here match the wire exactly. If you
 * change a DTO builder on the backend, change it here too — the compiler will
 * not otherwise catch the drift, because the response is `unknown` until cast.
 */

export type BloodRequest = {
  id: number;
  name: string;
  email: string;
  bloodGroup: string;
  units: number;
  district: string | null;
  thana: string | null;
  location: string | null;
  requiredDate: string | null;
  status: string | null;
  medicalReason: string | null;
  createdAt: string | null;
};

export type DoctorSummary = {
  id: number;
  name: string;
  specialization: string | null;
  degrees: string | null;
  experienceYears: number | null;
  zilla: string | null;
  hospitalName: string | null;
  consultationFee: string | null;
  profileImage: string | null;
  rating: string | null;
  bloodGroup: string | null;
};

export type DoctorProfile = DoctorSummary & {
  email: string | null;
  phoneNumber: string | null;
  address: string | null;
  hospitalAddress: string | null;
  description: string | null;
  languages: string | null;
  certifications: string | null;
  workingDays: string | null;
  workingHours: string | null;
  telemedicineAvailable: boolean | null;
};

export type Slot = {
  id: number;
  startTime: string | null;
  endTime: string | null;
  date: string | null;
  telemedicineAvailable: boolean | null;
  booked: boolean;
};

export type Appointment = {
  id: number;
  doctorId?: number | null;
  doctorName?: string | null;
  specialization?: string | null;
  appointmentDate: string | null;
  timeSlot: string | null;
  type: string | null;
  status: string | null;
};

export type PatientNotification = {
  id: number;
  message: string;
  date: string | null;
  read: boolean;
};

export type DashboardStats = {
  myRequests: number;
  pendingRequests: number;
  appointments: number;
  approvedDoctors: number;
  unreadNotifications: number;
};

export type Donor = {
  id: number;
  name: string;
  age: number;
  gender: string | null;
  bloodGroup: string | null;
  contact: string | null;
  email: string | null;
  status: string | null;
  lastDonated: string | null;
  location: string | null;
  district: string | null;
  thana: string | null;
};

// ---------------------------------------------------------------------------
// Fetchers
//
// Each tolerates a failure by returning an empty result rather than throwing,
// because these power dashboard-style pages where one broken panel should not
// take down the whole page. Callers that need to surface an error (a form
// submit) use springJson directly instead.
// ---------------------------------------------------------------------------

export async function fetchMyRequests(): Promise<BloodRequest[]> {
  try {
    return unwrap<BloodRequest>(
      await springJson("/patient/api/requests"),
    );
  } catch {
    return [];
  }
}

export async function fetchDoctors(): Promise<DoctorSummary[]> {
  try {
    return unwrap<DoctorSummary>(await springJson("/patient/api/doctors"));
  } catch {
    return [];
  }
}

export async function fetchDoctor(id: number): Promise<DoctorProfile | null> {
  try {
    const body = await springJson(`/patient/api/doctors/${id}`);
    const list = unwrap<DoctorProfile>(body);
    return list[0] ?? null;
  } catch {
    return null;
  }
}

export async function fetchAvailability(
  doctorId: number,
): Promise<Slot[]> {
  try {
    return unwrap<Slot>(
      await springJson(`/patient/api/doctors/${doctorId}/availability`),
    );
  } catch {
    return [];
  }
}

export async function fetchAppointments(): Promise<Appointment[]> {
  try {
    return unwrap<Appointment>(
      await springJson("/patient/api/appointments"),
    );
  } catch {
    return [];
  }
}

export async function fetchNotifications(): Promise<PatientNotification[]> {
  try {
    return unwrap<PatientNotification>(
      await springJson("/patient/api/notifications"),
    );
  } catch {
    return [];
  }
}

export async function fetchDashboard(): Promise<DashboardStats | null> {
  try {
    return await springJson<DashboardStats>("/patient/api/dashboard");
  } catch {
    return null;
  }
}

export async function fetchDonors(): Promise<Donor[]> {
  try {
    return unwrap<Donor>(await springJson("/patient/donor/api/list"));
  } catch {
    return [];
  }
}