"use server";

import { cookies } from "next/headers";

export type ChangePasswordState = { error?: string; message?: string } | undefined;

/**
 * Changes the signed-in admin's password, backing
 * admin_change_password.html.
 *
 * Admin is the one role with no "forgot password" flow — it requires the
 * current password, matching AdminController's Thymeleaf handler.
 */
export async function changeAdminPassword(
  _prev: ChangePasswordState,
  formData: FormData,
): Promise<ChangePasswordState> {
  const oldPassword = String(formData.get("oldPassword") ?? "");
  const newPassword = String(formData.get("newPassword") ?? "");
  const confirmPassword = String(formData.get("confirmPassword") ?? "");

  if (!oldPassword) return { error: "Please enter your current password." };
  if (!newPassword) return { error: "Please choose a new password." };
  if (newPassword !== confirmPassword) return { error: "Passwords do not match!" };

  const base = process.env.SPRING_API_URL ?? "http://localhost:8080";

  try {
    // The session cookie has to come along: the backend resolves the admin from
    // the SecurityContext, not from anything in the request body.
    const jar = await cookies();
    const session = jar.get("JSESSIONID")?.value;

    const res = await fetch(`${base}/admin/api/password/change`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(session ? { Cookie: `JSESSIONID=${session}` } : {}),
      },
      body: JSON.stringify({ oldPassword, newPassword, confirmPassword }),
      cache: "no-store",
    });

    if (res.status === 401 || res.status === 403) {
      return { error: "Your session has expired. Please sign in again." };
    }
    if (!res.ok) {
      return { error: "Something went wrong. Please try again." };
    }

    return await res.json();
  } catch {
    return { error: "Unable to reach the server. Please try again." };
  }
}