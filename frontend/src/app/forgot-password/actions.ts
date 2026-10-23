"use server";

/**
 * One-step patient password reset for the demo build.
 *
 * The old three-screen OTP wizard has been removed; the form sends the email
 * and new password directly to `/patient/api/password/reset`.
 */

export type ResetState = {
  error?: string;
  message?: string;
  done?: boolean;
} | undefined;

export async function resetPassword(
  _prev: ResetState,
  formData: FormData,
): Promise<ResetState> {
  const email = String(formData.get("email") ?? "").trim();
  const password = String(formData.get("password") ?? "");
  const confirmPassword = String(formData.get("confirmPassword") ?? "");

  if (!email) return { error: "Please provide a valid email address." };
  if (!password) return { error: "Please choose a new password." };
  if (password.length < 6) {
    return { error: "Password must be at least 6 characters." };
  }
  if (password !== confirmPassword) {
    return { error: "Passwords do not match!" };
  }

  const base = process.env.SPRING_API_URL ?? "http://localhost:8080";

  try {
    const res = await fetch(`${base}/patient/api/password/reset`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password, confirmPassword }),
      cache: "no-store",
    });

    const out = (await res.json().catch(() => null)) as
      | { success?: boolean; message?: string }
      | null;

    if (!res.ok || !out?.success) {
      return { error: out?.message ?? "Unable to reset password. Please try again." };
    }

    return { message: out.message ?? "Password successfully reset.", done: true };
  } catch {
    return { error: "Unable to reach the server. Please try again." };
  }
}
