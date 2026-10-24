"use server";

import { springFetch } from "@/lib/api";

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

export async function savePhoto(
  _prev: SaveState,
  formData: FormData,
): Promise<SaveState> {
  const file = formData.get("file");

  // Server Actions serialise their args through React's form encoding, so a
  // File survives the trip — but an empty picker still produces a File of
  // size 0, which the backend would reject with a vaguer message. Catching it
  // here keeps the message specific.
  if (!(file instanceof File) || file.size === 0) {
    return { ok: false, message: "Please choose a photo to upload." };
  }

  return postSection("/patient/api/health/photo", formData);
}