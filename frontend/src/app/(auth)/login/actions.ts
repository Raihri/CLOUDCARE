"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";

import { homeFor, LOGIN_PATH_BY_ROLE, type Role } from "@/lib/auth";
import { SPRING_API_URL } from "@/lib/api";

export type LoginState = { error?: string } | undefined;

/**
 * Log in against Spring's formLogin endpoint.
 *
 * Spring answers a successful formLogin with a 302 (not 200), so we must not
 * treat a redirect as success blindly — we verify by asking /api/me who we are
 * and capture the session cookie Spring just issued.
 */
export async function login(
  _prev: LoginState,
  formData: FormData,
): Promise<LoginState> {
  const email = String(formData.get("email") ?? "").trim();
  const password = String(formData.get("password") ?? "");
  const role = String(formData.get("role") ?? "PATIENT") as Role;

  if (!email || !password) {
    return { error: "Email and password are required." };
  }

  const base = SPRING_API_URL;
  const path = LOGIN_PATH_BY_ROLE[role] ?? LOGIN_PATH_BY_ROLE.PATIENT;

  // Every portal accepts the same credentials: email plus password.
  const credentials = new URLSearchParams({ email, password });

  const res = await fetch(`${base}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: credentials.toString(),
    redirect: "manual",
    cache: "no-store",
  });

  // Spring's formLogin rejects bad credentials with a 302 back to the login
  // page (or 401 when there's no default page configured).
  const failed = res.status === 401 || (res.status === 302 && !res.headers.get("set-cookie"));
  if (failed) {
    return { error: "Invalid email or password." };
  }

  const setCookie = res.headers.get("set-cookie");
  if (!setCookie) {
    return { error: "Sign-in failed: no session was created." };
  }

  const jar = await cookies();
  // Forward Spring's session cookie to the browser. Same-origin, so this stays
  // a first-party JSESSIONID and needs no SameSite=None handling.
  const session = /JSESSIONID=[^;]+/.exec(setCookie)?.[0];
  if (session) {
    jar.set("JSESSIONID", session.split("=")[1], {
      httpOnly: true,
      sameSite: "lax",
      secure: process.env.NODE_ENV === "production",
      path: "/",
    });
  }

  // Honour ?next= so a guarded deep link returns the user where they intended.
  const next = String(formData.get("next") ?? "");
  if (next.startsWith("/") && !next.startsWith("//")) {
    redirect(next);
  }
  redirect(homeFor(role));
}