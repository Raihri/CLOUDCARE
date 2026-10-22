"use server";

import { redirect } from "next/navigation";

export type RegisterState = { error?: string } | undefined;

/**
 * Register a patient account.
 *
 * Spring's /patient/register is a form endpoint that redirects to the OTP
 * verification page on success, or re-renders index.html with an
 * `errorMessage`. We post form-encoded and treat the redirect target as
 * success, lifting errorMessage out of the returned HTML when we get one.
 */
export async function register(
  _prev: RegisterState,
  formData: FormData,
): Promise<RegisterState> {
  const name = String(formData.get("name") ?? "").trim();
  const username = String(formData.get("username") ?? "").trim();
  const email = String(formData.get("email") ?? "").trim();
  const password = String(formData.get("password") ?? "");
  const confirm = String(formData.get("confirmPassword") ?? "");

  if (!name || !username || !email || !password) {
    return { error: "All fields are required." };
  }
  if (username.length < 3 || username.length > 50) {
    return { error: "Username must be between 3 and 50 characters." };
  }
  if (password.length < 6) {
    return { error: "Password must be at least 6 characters." };
  }
  if (password !== confirm) {
    return { error: "Passwords do not match." };
  }

  const base = process.env.SPRING_API_URL ?? "http://localhost:8080";

  const res = await fetch(`${base}/patient/register`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      name,
      username,
      email,
      password,
      confirmPassword: confirm,
    }).toString(),
    redirect: "manual",
    cache: "no-store",
  });

  // Success redirects to the OTP page. Anything else re-renders the form page
  // with an errorMessage we can surface.
  if (res.status === 302) {
    const location = res.headers.get("location") ?? "";
    if (location.includes("/patient/verify")) {
      redirect(`/verify?email=${encodeURIComponent(email)}`);
    }
    if (location.includes("/patient/forgot-password")) {
      return {
        error:
          "That email is already registered but not verified. Reset your password to continue.",
      };
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