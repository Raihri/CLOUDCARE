"use server";

import { revalidatePath } from "next/cache";

import { springFetch } from "@/lib/api";

export type PatientUpdateState = { ok: boolean; message: string } | undefined;

/**
 * Save the doctor's edits to a patient chart.
 *
 * The server-side Spring helper forwards the current JSESSIONID, so the
 * backend's doctor role check sees the same session as the page render.
 */
export async function updatePatient(
  _prev: PatientUpdateState,
  formData: FormData,
): Promise<PatientUpdateState> {
  const patientId = String(formData.get("patientId") ?? "");
  if (!/^\d+$/.test(patientId)) {
    return { ok: false, message: "That patient ID isn't valid." };
  }

  try {
    const res = await springFetch(`/doctor/api/patients/${patientId}`, {
      method: "POST",
      body: formData,
    });
    const data = (await res.json().catch(() => null)) as
      | { success?: boolean; message?: string }
      | null;

    if (!res.ok || data?.success === false) {
      return {
        ok: false,
        message: data?.message ?? `Could not save (${res.status}).`,
      };
    }

    revalidatePath(`/doctor/patients/${patientId}`);
    revalidatePath("/doctor/patients");
    revalidatePath("/doctor/dashboard");
    return { ok: true, message: data?.message ?? "Patient record updated." };
  } catch {
    return { ok: false, message: "Could not reach the server. Please try again." };
  }
}
