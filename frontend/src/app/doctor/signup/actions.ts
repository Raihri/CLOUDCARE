"use server";
import { SPRING_API_URL } from "@/lib/api";

export type DoctorSignupState =
  | { error?: string; success?: boolean }
  | undefined;

/**
 * Submit a doctor application.
 *
 * Spring expects multipart/form-data with two required files
 * (profileImageFile, certificateFile), so this builds FormData and must NOT set
 * a Content-Type header — the runtime adds the multipart boundary itself.
 * The endpoint answers JSON `{ success, message }`.
 */
export async function applyAsDoctor(
  _prev: DoctorSignupState,
  formData: FormData,
): Promise<DoctorSignupState> {
  const name = String(formData.get("name") ?? "").trim();
  const email = String(formData.get("email") ?? "").trim();
  const password = String(formData.get("password") ?? "");
  const bmdcRegNo = String(formData.get("bmdcRegNo") ?? "").trim();

  if (!name || !email || !password || !bmdcRegNo) {
    return { error: "Name, email, password and BMDC registration number are required." };
  }
  if (password.length < 6) {
    return { error: "Password must be at least 6 characters." };
  }

  const profile = formData.get("profileImageFile");
  const certificate = formData.get("certificateFile");

  if (!(profile instanceof File) || profile.size === 0) {
    return { error: "A profile photo is required." };
  }
  if (!(certificate instanceof File) || certificate.size === 0) {
    return { error: "A supporting certificate is required." };
  }

  // Pass the incoming files straight through; re-wrapping File objects in a
  // new FormData is unnecessary and risks losing the filename.
  const payload = new FormData();
  payload.set("name", name);
  payload.set("email", email);
  payload.set("password", password);
  payload.set("bmdcRegNo", bmdcRegNo);
  payload.set("specialization", String(formData.get("specialization") ?? ""));
  payload.set("degrees", String(formData.get("degrees") ?? ""));
  payload.set("phoneNumber", String(formData.get("phoneNumber") ?? ""));
  payload.set("profileImageFile", profile);
  payload.set("certificateFile", certificate);

  try {
    const res = await fetch(`${SPRING_API_URL}/doctor/signup/request`, {
      method: "POST",
      body: payload,
      cache: "no-store",
    });
    const out = (await res.json()) as { success?: boolean; message?: string };
    if (!out.success) {
      return { error: out.message ?? "Could not submit your application." };
    }
    return { success: true };
  } catch {
    return { error: "Could not reach the server. Please try again." };
  }
}
