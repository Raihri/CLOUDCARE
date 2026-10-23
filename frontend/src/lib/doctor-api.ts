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

export type DoctorPrescription = {
  id: number;
  patientId: number | null;
  patientName: string | null;
  patientEmail: string | null;
  doctorId: number | null;
  doctorName: string | null;
  doctorSpecialization: string | null;
  hospitalName: string | null;
  diagnosis: string | null;
  medicines: string;
  instructions: string | null;
  followUpDate: string | null;
  createdAt: string | null;
  daysUntilFollowUp: number | null;
  followUpStatus: "NONE" | "OVERDUE" | "DUE_TODAY" | "UPCOMING" | "SCHEDULED";
};

/** Full chart returned by GET /doctor/api/patients/{id}. */
export type DoctorPatientRecord = {
  success: boolean;
  id: number;
  name: string | null;
  email: string | null;
  photoUrl: string | null;
  bloodGroup: string | null;
  age: number | null;
  gender: string | null;
  dateOfBirth: string | null;
  maritalStatus: string | null;
  nationalId: string | null;
  emergencyContact: string | null;
  division: string | null;
  zilla: string | null;
  address: string | null;
  weight: number | null;
  height: number | null;
  bloodPressure: string | null;
  systolicBP: number | null;
  diastolicBP: number | null;
  heartRate: number | null;
  oxygenSaturation: number | null;
  respiratoryRate: number | null;
  bodyTemperature: number | null;
  hb: number | null;
  rbc: number | null;
  wbc: number | null;
  platelet: number | null;
  sugarLevel: number | null;
  bloodSugarLevel: number | null;
  cholesterolTotal: number | null;
  cholesterolLDL: number | null;
  cholesterolHDL: number | null;
  triglyceride: number | null;
  creatinine: number | null;
  creatinineLevel: number | null;
  alt: number | null;
  ast: number | null;
  bilirubin: number | null;
  rhFactor: string | null;
  bmi: number | null;
  bmr: number | null;
  cholesterol: number | null;
  bloodSugar: number | null;
  sleepHours: number | null;
  waterIntake: number | null;
  smoker: boolean | null;
  alcoholConsumer: boolean | null;
  physicalActivity: number | null;
  stressLevel: number | null;
  anxietyScore: number | null;
  allergies: string[];
  diseases: string[];
  medications: string[];
  symptoms: string[];
  familyDiseaseHistory: string[];
  vaccinationHistory: string[];
  pastSurgeries: string[];
  mentalDiseases: string[];
  updatedAt: string | null;
  myPatient: boolean;
  appointmentCount: number;
  appointments: DoctorAppointment[];
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

export async function fetchDoctorPatient(
  patientId: number,
): Promise<DoctorPatientRecord | null> {
  try {
    const body = await springJson<DoctorPatientRecord>(
      `/doctor/api/patients/${patientId}`,
    );
    return body?.success ? body : null;
  } catch {
    return null;
  }
}

export async function fetchDoctorPrescriptions(
  patientId?: number,
): Promise<DoctorPrescription[]> {
  try {
    const query = patientId ? `?patientId=${patientId}` : "";
    return unwrap<DoctorPrescription>(
      await springJson(`/doctor/api/prescriptions${query}`),
    );
  } catch {
    return [];
  }
}