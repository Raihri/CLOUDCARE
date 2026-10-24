"use server";

import { redirect } from "next/navigation";

import { ApiError, springJson } from "@/lib/api";
import { BLOOD_GROUPS } from "@/lib/blood-groups";

export type RequestFormState = {
  error?: string;
  matchedCount?: number;
} | undefined;

/**
 * Post a blood request.
 *
 * The backend validates name/email/bloodGroup/units and responds 400 with a
 * `message` on failure; springJson turns that into an ApiError whose message
 * we surface directly, so the user sees the backend's own wording.
 */
export async function createBloodRequest(
  _prev: RequestFormState,
  formData: FormData,
): Promise<RequestFormState> {
  const name = String(formData.get("name") ?? "").trim();
  const email = String(formData.get("email") ?? "").trim();
  const bloodGroup = String(formData.get("bloodGroup") ?? "").trim();
  const units = Number(formData.get("units") ?? 0);
  const district = String(formData.get("district") ?? "").trim();
  const thana = String(formData.get("thana") ?? "").trim();
  const medicalReason = String(formData.get("medicalReason") ?? "").trim();
  const requiredDate = String(formData.get("requiredDate") ?? "").trim();

  if (!name || !email || !bloodGroup) {
    return { error: "Name, email and blood group are required." };
  }
  if (!(BLOOD_GROUPS as readonly string[]).includes(bloodGroup)) {
    return { error: "Choose a valid blood group." };
  }
  if (!Number.isFinite(units) || units < 1) {
    return { error: "Units must be at least 1." };
  }

  let matchedCount = 0;
  try {
    const result = await springJson<{ matchedCount?: number }>(
      "/patient/request/api/create",
      {
        method: "POST",
        body: JSON.stringify({
          name,
          email,
          bloodGroup,
          units,
          district: district || null,
          thana: thana || null,
          medicalReason: medicalReason || null,
          requiredDate: requiredDate || null,
        }),
      },
    );
    matchedCount = result?.matchedCount ?? 0;
  } catch (err) {
    if (err instanceof ApiError) {
      return { error: err.message };
    }
    return { error: "Could not submit your request. Please try again." };
  }

  // Deliberately OUTSIDE the try block: redirect() signals via a thrown
  // control-flow exception, so calling it inside would be caught above and
  // turned into a misleading "please try again" error.
  redirect(`/requests?created=1&matched=${matchedCount}`);
}