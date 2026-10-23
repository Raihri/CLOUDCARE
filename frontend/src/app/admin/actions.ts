"use server";

import { revalidatePath } from "next/cache";

import { springFetch } from "@/lib/api";

export type AdminActionState = { message: string; ok: boolean } | undefined;

/** Approve or reject a doctor application. */
export async function setDoctorStatus(
  _prev: AdminActionState,
  formData: FormData,
): Promise<AdminActionState> {
  const doctorId = String(formData.get("doctorId") ?? "");
  const status = String(formData.get("status") ?? "");
  const reason = String(formData.get("reason") ?? "");

  if (!doctorId || !["APPROVED", "REJECTED"].includes(status)) {
    return { ok: false, message: "That action wasn't recognised." };
  }

  const params = new URLSearchParams({ status });
  // Only meaningful on rejection, but harmless either way — the backend falls
  // back to a generic reason when this is blank.
  if (reason) params.set("reason", reason);

  try {
    const res = await springFetch(
      `/admin/doctor/${encodeURIComponent(doctorId)}/status`,
      { method: "PATCH", body: params },
    );
    const data = await res.json().catch(() => null);

    if (!res.ok || data?.success === false) {
      return {
        ok: false,
        message: data?.message ?? `Request failed (${res.status}).`,
      };
    }

    revalidatePath("/admin/doctors");
    revalidatePath("/admin/doctors/pending");
    revalidatePath("/admin/dashboard");
    revalidatePath("/doctors");
    return {
      ok: true,
      message: data?.message ?? `Doctor marked ${status.toLowerCase()}.`,
    };
  } catch {
    return { ok: false, message: "Could not reach the server. Please try again." };
  }
}

/** Delete a feedback entry. */
export async function deleteFeedback(
  _prev: AdminActionState,
  formData: FormData,
): Promise<AdminActionState> {
  const id = String(formData.get("feedbackId") ?? "");
  if (!id) return { ok: false, message: "No feedback selected." };

  try {
    const res = await springFetch(
      `/admin/feedbacks/api/${encodeURIComponent(id)}`,
      { method: "DELETE" },
    );
    const data = await res.json().catch(() => null);

    if (!res.ok || data?.success === false) {
      return {
        ok: false,
        message: data?.message ?? `Request failed (${res.status}).`,
      };
    }

    revalidatePath("/admin/feedbacks");
    return { ok: true, message: data?.message ?? "Feedback deleted." };
  } catch {
    return { ok: false, message: "Could not reach the server. Please try again." };
  }
}