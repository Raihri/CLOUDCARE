"use server";

import { springFetch } from "@/lib/api-client";
import { revalidatePath } from "next/cache";

export type ActionState = { message: string; ok: boolean } | undefined;

/**
 * Confirm or cancel an appointment, then refresh the pages that show it.
 *
 * revalidatePath rather than a redirect: these pages are panels on a dashboard
 * and a full navigation would throw away scroll position.
 */
export async function setAppointmentStatus(
  _prev: ActionState,
  formData: FormData,
): Promise<ActionState> {
  const id = String(formData.get("appointmentId") ?? "");
  const action = String(formData.get("action") ?? "");
  if (!id || (action !== "confirm" && action !== "cancel")) {
    return { ok: false, message: "That action wasn't recognised." };
  }

  try {
    const res = await springFetch(
      `/doctor/api/appointments/${encodeURIComponent(id)}/${action}`,
      { method: "POST" },
    );
    const data = await res.json().catch(() => null);

    if (!res.ok || data?.success === false) {
      return { ok: false, message: data?.message ?? `Request failed (${res.status}).` };
    }

    revalidatePath("/doctor/appointments");
    revalidatePath("/doctor/dashboard");
    return { ok: true, message: data?.message ?? "Appointment updated." };
  } catch {
    return { ok: false, message: "Could not reach the server. Please try again." };
  }
}