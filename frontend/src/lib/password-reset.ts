"use server";

import { SPRING_API_URL } from "@/lib/api";

/**
 * Server Actions for the emailed-link password reset (patients and doctors).
 *
 * The backend never returns the reset token: it only goes out by email. These
 * actions just relay the two steps, "send me a link" and "set a new password
 * with this token", to `/{role}/api/password/*`.
 */

export type ResetRole = "patient" | "doctor";

export type ResetState =
  | { error?: string; message?: string; done?: boolean }
  | undefined;

const MIN_PASSWORD_LENGTH = 8;

async function post(
  path: string,
  body: Record<string, string>,
): Promise<{ success?: boolean; message?: string }> {
  try {
    const res = await fetch(`${SPRING_API_URL}${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
      cache: "no-store",
    });
    if (!res.ok) return { success: false, message: "Something went wrong. Please try again." };
    return (await res.json()) as { success?: boolean; message?: string };
  } catch {
    return { success: false, message: "Unable to reach the server. Please try again." };
  }
}

export async function requestPasswordReset(
  role: ResetRole,
  _prev: ResetState,
  formData: FormData,
): Promise<ResetState> {
  const email = String(formData.get("email") ?? "").trim();
  if (!email) return { error: "Please provide a valid email address." };

  const out = await post(`/${role}/api/password/forgot`, { email });
  if (!out.success) return { error: out.message };
  return { message: out.message, done: true };
}

export async function setNewPassword(
  role: ResetRole,
  _prev: ResetState,
  formData: FormData,
): Promise<ResetState> {
  const token = String(formData.get("token") ?? "").trim();
  const password = String(formData.get("password") ?? "");
  const confirmPassword = String(formData.get("confirmPassword") ?? "");

  if (!token) return { error: "This reset link is invalid. Please request a new one." };
  if (password.length < MIN_PASSWORD_LENGTH) {
    return { error: `Password must be at least ${MIN_PASSWORD_LENGTH} characters.` };
  }
  if (password !== confirmPassword) return { error: "Passwords do not match." };

  const out = await post(`/${role}/api/password/reset`, { token, password, confirmPassword });
  if (!out.success) return { error: out.message };
  return { message: out.message, done: true };
}
