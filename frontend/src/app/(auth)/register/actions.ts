"use server";

import { redirect } from "next/navigation";
import { SPRING_API_URL } from "@/lib/api";

export type RegisterState = { error?: string } | undefined;

/**
 * Register a patient account.
 *
 * `/patient/register` creates an enabled account immediately (there is no
 * email OTP step) and answers JSON `{ success, message }`.
 */
export async function register(
  _prev: RegisterState,
  formData: FormData,
): Promise<RegisterState> {
  const name = String(formData.get("name") ?? "").trim();
  const email = String(formData.get("email") ?? "").trim();
  const password = String(formData.get("password") ?? "");
  const confirm = String(formData.get("confirmPassword") ?? "");

  if (!name || !email || !password) {
    return { error: "All fields are required." };
  }
  if (password.length < 6) {
    return { error: "Password must be at least 6 characters." };
  }
  if (password !== confirm) {
    return { error: "Passwords do not match." };
  }

  let out: { success?: boolean; message?: string };
  try {
    const res = await fetch(`${SPRING_API_URL}/patient/register`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        name,
        email,
        password,
        confirmPassword: confirm,
      }).toString(),
      cache: "no-store",
    });
    out = await res.json();
  } catch {
    return { error: "Could not reach the server. Please try again." };
  }

  if (!out.success) {
    return { error: out.message ?? "Registration failed. Please try again." };
  }

  redirect(`/login?registered=1&email=${encodeURIComponent(email)}`);
}
