import "server-only";

import { springJson, unwrap } from "@/lib/api";

/**
 * Types for the admin JSON API.
 *
 * Covers the endpoints added to admin_controller (dashboard, doctors,
 * donor-summary) and AdminFeedbackController (feedback list/delete).
 */

export type AdminDoctor = {
  id: number;
  name: string | null;
  email: string | null;
  bmdcRegNo: string | null;
  specialization: string | null;
  degrees: string | null;
  hospitalName: string | null;
  zilla: string | null;
  profileImage: string | null;
  experienceYears: number | null;
  status: string | null;
};

export type AdminDashboard = {
  success: boolean;
  totalDoctors: number;
  pendingDoctors: number;
  approvedDoctors: number;
  rejectedDoctors: number;
};

export type AdminDonorSummary = {
  success: boolean;
  totalDonors: number;
  eligibleDonors: number;
  byBloodGroup: Record<string, number>;
};

export type AdminFeedback = {
  id: number;
  name: string | null;
  email: string | null;
  category: string | null;
  rating: number | null;
  comments: string | null;
  createdAt: string | null;
  createdDay: string | null;
};

/** Donor/reques trends from AdminControllerDonor, plus the urgent list. */
export type DonorSummary = {
  success: boolean;
  totalDonors: number;
  eligibleDonors: number;
  pendingRequests: number;
  matchedRequests: number;
  byBloodGroup: Record<string, number>;
};

export type UrgentRequest = {
  id: number;
  name: string | null;
  bloodGroup: string | null;
  units: number | null;
  requiredDate: string | null;
  location: string | null;
};

export async function fetchAdminDashboard(): Promise<AdminDashboard | null> {
  try {
    return await springJson<AdminDashboard>("/admin/api/dashboard");
  } catch {
    return null;
  }
}

export async function fetchAdminDoctors(
  status = "ALL",
): Promise<AdminDoctor[]> {
  try {
    return unwrap<AdminDoctor>(
      await springJson(`/admin/api/doctors?status=${encodeURIComponent(status)}`),
    );
  } catch {
    return [];
  }
}

export async function fetchAdminFeedbacks(
  filters: Record<string, string> = {},
): Promise<AdminFeedback[]> {
  try {
    const params = new URLSearchParams();
    for (const [k, v] of Object.entries(filters)) {
      if (v) params.set(k, v);
    }
    const qs = params.toString();
    return unwrap<AdminFeedback>(
      await springJson(`/admin/feedbacks/api${qs ? `?${qs}` : ""}`),
    );
  } catch {
    return [];
  }
}

export async function fetchDonorSummary(): Promise<DonorSummary | null> {
  try {
    return await springJson<DonorSummary>("/admin/donor/api/summary");
  } catch {
    return null;
  }
}

export async function fetchUrgentRequests(): Promise<UrgentRequest[]> {
  try {
    return unwrap<UrgentRequest>(
      await springJson("/admin/donor/api/requests/urgent"),
    );
  } catch {
    return [];
  }
}