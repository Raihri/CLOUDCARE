"use server";

import { revalidatePath } from "next/cache";

import { springFetch } from "@/lib/api-client";

export type SlotActionState = { message: string; ok: boolean } | undefined;

export async function addSlot(
  _prev: SlotActionState,
  formData: FormData,
): Promise<SlotActionState> {
  const date = String(formData.get("date") ?? "");
  const startTime = String(formData.get("startTime") ?? "");
  const endTime = String(formData.get("endTime") ?? "");

  // Checked here as well as on the server so an obviously inverted range is
  // caught before the round-trip; the server re-checks and wins either way.
  if (startTime && endTime && startTime >= endTime) {
    return { ok: false, message: "The start time must be before the end time." };
  }
  if (!date || !startTime || !endTime) {
    return { ok: false, message: "Date, start and end are all required." };
  }

  try {
    const res = await springFetch("/doctor/api/availability", {
      method: "POST",
      body: formData,
    });
    const data = await res.json().catch(() => null);

    if (!res.ok || data?.success === false) {
      return {
        ok: false,
        message: data?.message ?? `Could not add the slot (${res.status}).`,
      };
    }

    revalidatePath("/doctor/availability");
    revalidatePath("/doctor/dashboard");
    return { ok: true, message: data?.message ?? "Slot added." };
  } catch {
    return { ok: false, message: "Could not reach the server. Please try again." };
  }
}

export async function removeSlot(
  _prev: SlotActionState,
  formData: FormData,
): Promise<SlotActionState> {
  const slotId = String(formData.get("slotId") ?? "");
  if (!slotId) return { ok: false, message: "No slot selected." };

  try {
    const res = await springFetch(
      `/doctor/api/availability/${encodeURIComponent(slotId)}`,
      { method: "DELETE" },
    );
    const data = await res.json().catch(() => null);

    if (!res.ok || data?.success === false) {
      return {
        ok: false,
        message: data?.message ?? `Could not remove the slot (${res.status}).`,
      };
    }

    revalidatePath("/doctor/availability");
    revalidatePath("/doctor/dashboard");
    return { ok: true, message: data?.message ?? "Slot removed." };
  } catch {
    return { ok: false, message: "Could not reach the server. Please try again." };
  }
}