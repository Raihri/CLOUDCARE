"use server";

/**
 * Server actions for the patient password-reset flow, backing
 * forgot_password.html / otp_reset.html / reset_password.html.
 *
 * The three legacy templates were one wizard split across three views. Here
 * the same three steps are three actions, and the page keeps the step in the URL
 * so the browser Back button behaves the way users expect.
 */

export type ResetState = {
  error?: string;
  message?: string;
  /** Present after a successful verify, so the next step knows who to reset. */
  email?: string;
  /** Set once the password is actually changed, so the page can offer login. */
  done?: boolean;
} | undefined;

async function post(
  path: string,
  payload: Record<string, string>,
): Promise<{ success: boolean; message?: string; email?: string }> {
  const base = process.env.SPRING_API_URL ?? "http://localhost:8080";

  try {
    const res = await fetch(`${base}${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
      cache: "no-store",
    });

    if (!res.ok) {
      return { success: false, message: "Something went wrong. Please try again." };
    }
    return await res.json();
  } catch {
    return { success: false, message: "Unable to reach the server. Please try again." };
  }
}

/** Step 1 — ask for a verification code. */
export async function requestResetCode(
  _prev: ResetState,
  formData: FormData,
): Promise<ResetState> {
  const email = String(formData.get("email") ?? "").trim();
  if (!email) return { error: "Please provide a valid email address." };

  const out = await post("/patient/api/password/forgot", { email });
  if (!out.success) return { error: out.message };

  // Deliberately uniform whether or not the address is registered.
  return { message: out.message, email };
}

/** Step 2 — verify the emailed code. */
export async function verifyResetCode(
  _prev: ResetState,
  formData: FormData,
): Promise<ResetState> {
  const email = String(formData.get("email") ?? "").trim();
  const code = String(formData.get("code") ?? "").trim();

  if (!code) return { error: "Please enter the 6-digit code.", email };

  const out = await post("/patient/api/password/verify", { email, code });
  if (!out.success) return { error: out.message, email };

  return { message: "Code verified.", email: out.email ?? email };
}

/** Step 3 — set the new password. */
export async function setNewPassword(
  _prev: ResetState,
  formData: FormData,
): Promise<ResetState> {
  const email = String(formData.get("email") ?? "").trim();
  const password = String(formData.get("password") ?? "");
  const confirmPassword = String(formData.get("confirmPassword") ?? "");

  if (!email) return { error: "Email information missing. Please try again.", done: false };
  if (!password) return { error: "Please choose a new password.", email, done: false };
  if (password !== confirmPassword) {
    return { error: "Passwords do not match!", email, done: false };
  }

  const out = await post("/patient/api/password/reset", {
    email,
    password,
    confirmPassword,
  });
  if (!out.success) return { error: out.message, email, done: false };

  return { message: out.message, done: true };
}