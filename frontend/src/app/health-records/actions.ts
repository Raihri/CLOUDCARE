"use server";

import { springFetch } from "@/lib/api-client";

export type SaveState = {
  message: string;
  ok: boolean;
} | undefined;

/** Shared submit helper. Posts form-encoded to one of the health endpoints and
 *  normalises both failure modes (network error, and `success: false` with a
 *  backend message) into a single state shape. */
async function postSection(
  path: string,
  formData: FormData,
): Promise<SaveState> {
  try {
    const res = await springFetch(path, { method: "POST", body: formData });
    const data = await res.json().catch(() => null);

    if (!res.ok || data?.success === false) {
      return {
        ok: false,
        message: data?.message ?? `Could not save (${res.status}).`,
      };
    }
    return { ok: true, message: data?.message ?? "Saved." };
  } catch {
    return { ok: false, message: "Could not reach the server. Please try again." };
  }
}

export async function savePersonal(
  _prev: SaveState,
  formData: FormData,
): Promise<SaveState> {
  return postSection("/patient/api/health/personal", formData);
}

export async function saveVitals(
  _prev: SaveState,
  formData: FormData,
): Promise<SaveState> {
  return postSection("/patient/api/health/vitals", formData);
}

export async function saveLifestyle(
  _prev: SaveState,
  formData: FormData,
): Promise<SaveState> {
  return postSection("/patient/api/health/lifestyle", formData);
}

export async function saveMenstrual(
  _prev: SaveState,
  formData: FormData,
): Promise<SaveState> {
  return postSection("/patient/api/health/menstrual", formData);
}

export async function saveEnvironment(
  _prev: SaveState,
  formData: FormData,
): Promise<SaveState> {
  return postSection("/patient/api/health/environment", formData);
}