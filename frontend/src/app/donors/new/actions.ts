"use server";

import { redirect } from "next/navigation";

import { ApiError, springJson } from "@/lib/api";
import { BLOOD_GROUPS } from "@/lib/blood-groups";

export type DonorFormState = {
  error?: string;
  eligible?: boolean;
  reasons?: string[];
} | undefined;

/**
 * Register the signed-in patient as a donor.
 *
 * On success the backend returns an `eligibility` object rather than throwing:
 * a donor under 18, or one who donated in the last 3 months, is still saved but
 * flagged ineligible with reasons. We surface those rather than treating a
 * non-eligible registration as a failure.
 */
export async function registerDonor(
  _prev: DonorFormState,
  formData: FormData,
): Promise<DonorFormState> {
  const name = String(formData.get("name") ?? "").trim();
  const age = Number(formData.get("age") ?? 0);
  const gender = String(formData.get("gender") ?? "").trim();
  const bloodGroup = String(formData.get("bloodGroup") ?? "").trim();
  const contact = String(formData.get("contact") ?? "").trim();
  const district = String(formData.get("district") ?? "").trim();
  const thana = String(formData.get("thana") ?? "").trim();
  const lastDonated = String(formData.get("lastDonated") ?? "").trim();

  if (!name || !contact || !bloodGroup) {
    return { error: "Name, contact number and blood group are required." };
  }
  if (!(BLOOD_GROUPS as readonly string[]).includes(bloodGroup)) {
    return { error: "Choose a valid blood group." };
  }
  if (!Number.isFinite(age) || age < 18) {
    return { error: "Donors must be at least 18 years old." };
  }
  if (lastDonated) {
    const parsed = new Date(lastDonated);
    if (Number.isNaN(parsed.getTime())) {
      return { error: "Enter a valid last-donation date." };
    }
    if (parsed.getTime() > Date.now()) {
      return { error: "Last donated date cannot be in the future." };
    }
  }

  let eligibility: { eligible?: boolean; reasons?: string[] } = {};

  try {
    const result = await springJson<{
      eligibility?: { eligible?: boolean; reasons?: string[] };
    }>("/patient/donor/api/create", {
      method: "POST",
      body: JSON.stringify({
        name,
        age,
        gender: gender || null,
        bloodGroup,
        contact,
        district: district || null,
        thana: thana || null,
        lastDonated: lastDonated || null,
      }),
    });
    eligibility = result?.eligibility ?? {};
  } catch (err) {
    if (err instanceof ApiError) {
      return { error: err.message };
    }
    return { error: "Could not register you as a donor. Please try again." };
  }

  // Outside the try block — redirect() signals by throwing, so calling it
  // inside would be caught above and misreported as a failure.
  if (eligibility.eligible === false) {
    redirect(
      `/donors?registered=1&eligible=0&reasons=${encodeURIComponent(
        (eligibility.reasons ?? []).join("; "),
      )}`,
    );
  }

  redirect("/donors?registered=1&eligible=1");
}