"use server";

import { revalidatePath } from "next/cache";

import { springFetch } from "@/lib/api-client";

export type ProfileState = { message: string; ok: boolean } | undefined;

export async function saveProfile(
  _prev: ProfileState,
  formData: FormData,
): Promise<ProfileState> {
  try {
    const res = await springFetch("/doctor/api/profile", {
      method: "POST",
      body: formData,
    });
    const data = await res.json().catch(() => null);

    if (!res.ok || data?.success === false) {
      return {
        ok: false,
        message: data?.message ?? `Could not save (${res.status}).`,
      };
    }

    revalidatePath("/doctor/profile");
    revalidatePath("/doctor/dashboard");
    // The public doctor listing is built from the same row, so refresh it too.
    revalidatePath("/doctors");
    return { ok: true, message: data?.message ?? "Profile saved." };
  } catch {
    return { ok: false, message: "Could not reach the server. Please try again." };
  }
}