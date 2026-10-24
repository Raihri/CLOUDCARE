"use server";

import { redirect } from "next/navigation";
import { SPRING_API_URL } from "@/lib/api";

export type RegisterState = { error?: string } | undefined;

/**
 * Register a patient account.
 *
 * Spring's `/patient/register` creates an enabled account immediately (email
 * OTP has been removed) and redirects to the patient login page. The Next.js
 * action forwards that success to the shared login screen with a notice.
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

  const base = SPRING_API_URL;

  const res = await fetch(`${base}/patient/register`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      name,
      email,
      password,
      confirmPassword: confirm,
    }).toString(),
    redirect: "manual",
    cache: "no-store",
  });

  if (res.status === 302) {
    const location = res.headers.get("location") ?? "";
    if (location.includes("/patient/login")) {
      redirect(`/login?registered=1&email=${encodeURIComponent(email)}`);
    }
  }

  const html = await res.text();
  const backendError = extractError(html);
  return { error: backendError ?? "Registration failed. Please try again." };
}

/** Pull errorMessage out of the Thymeleaf page Spring re-renders. */
function extractError(html: string): string | null {
  const match =
    /class="error[^"]*"[^>]*>([\s\S]{0,300}?)<\/div>/i.exec(html) ??
    /errorMessage[^>]*>([\s\S]{0,300}?)<\/div>/i.exec(html);

  if (!match) return null;

  return match[1]
    .replace(/<[^>]+>/g, " ")
    .replace(/\s+/g, " ")
    .trim()
    .slice(0, 200);
}
