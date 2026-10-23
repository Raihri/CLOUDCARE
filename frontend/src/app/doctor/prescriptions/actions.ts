"use server";

import { revalidatePath } from "next/cache";

import { springFetch } from "@/lib/api";

export type PrescriptionState = { ok: boolean; message: string } | undefined;

/**
 * Save a prescription through the doctor JSON API.
 *
 * FormData is passed through unchanged so Spring's @RequestParam binding sees
 * the same field names as the form. springFetch forwards the JSESSIONID, which
 * is what keeps the /doctor/api/** role check satisfied.
 */
export async function createPrescription(
  _prev: PrescriptionState,
  formData: FormData,
): Promise<PrescriptionState> {
  const patientId = String(formData.get("patientId") ?? "").trim();
  const medicines = String(formData.get("medicines") ?? "").trim();
  const followUpDate = String(formData.get("followUpDate") ?? "").trim();

  if (!/^\d+$/.test(patientId)) {
    return { ok: false, message: "Choose a patient before writing a prescription." };
  }
  if (!medicines) {
    return { ok: false, message: "Enter at least one medicine or treatment." };
  }
  if (!followUpDate) {
    return { ok: false, message: "Choose the follow-up date." };
  }

  try {
    const res = await springFetch("/doctor/api/prescriptions", {
      method: "POST",
      body: formData,
    });
    const data = (await res.json().catch(() => null)) as
      | { success?: boolean; message?: string }
      | null;

    if (!res.ok || data?.success === false) {
      return {
        ok: false,
        message: data?.message ?? `Could not save the prescription (${res.status}).`,
      };
    }

    revalidatePath("/doctor/prescriptions");
    revalidatePath(`/doctor/patients/${patientId}`);
    revalidatePath("/doctor/dashboard");
    return {
      ok: true,
      message: data?.message ?? "Prescription saved. The patient has been notified.",
    };
  } catch {
    return {
      ok: false,
      message: "Could not reach the server. Please try again.",
    };
  }
}
