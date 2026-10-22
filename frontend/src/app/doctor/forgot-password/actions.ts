"use server";

/**
 * Server actions for the doctor password reset, backing
 * doctor_forgot_password.html and doctor_reset_password.html.
 *
 * Doctors use a stored token emailed as a link, not the emailed-OTP flow the
 * patient side uses, so these are the two-step token equivalents.
 */

export type DoctorResetState = {
  error?: string;
  message?: string;
  /** Echoed back so step 2 can submit it without re-typing. */
  token?: string;
  done?: boolean;
} | undefined;

async function call(
  path: string,
  init: RequestInit,
): Promise<{ success: boolean; message?: string; token?: string }> {
  const base = process.env.SPRING_API_URL ?? "http://localhost:8080";
  try {
    const res = await fetch(`${base}${path}`, { ...init, cache: "no-store" });
    if (!res.ok) {
      return { success: false, message: "Something went wrong. Please try again." };
    }
    return await res.json();
  } catch {
    return { success: false, message: "Unable to reach the server. Please try again." };
  }
}

export async function requestDoctorReset(
  _prev: DoctorResetState,
  formData: FormData,
): Promise<DoctorResetState> {
  const email = String(formData.get("email") ?? "").trim();
  if (!email) return { error: "Please provide a valid email address." };

  const out = await call("/doctor/api/password/forgot", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
  });

  if (!out.success) return { error: out.message };
  return { message: out.message, token: out.token };
}

export async function setDoctorPassword(
  _prev: DoctorResetState,
  formData: FormData,
): Promise<DoctorResetState> {
  const token = String(formData.get("token") ?? "").trim();
  const password = String(formData.get("newPassword") ?? "");
  const confirm = String(formData.get("confirmPassword") ?? "");

  if (!token) return { error: "Invalid or expired password reset link." };
  if (!password) return { error: "Please choose a new password." };
  if (password !== confirm) return { error: "Passwords do not match!", token };

  const out = await call("/doctor/api/password/reset", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ token, newPassword: password }),
  });

  if (!out.success) return { error: out.message, token };
  return { message: out.message, done: true };
}