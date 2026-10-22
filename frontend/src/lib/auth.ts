import "server-only";

import { redirect } from "next/navigation";

import { ApiError, springJson } from "@/lib/api";

export type Role = "PATIENT" | "DOCTOR" | "ADMIN";

export type CurrentUser = {
  id: number;
  name: string;
  email: string;
  role: Role;
  avatarUrl?: string | null;
};

/** Where each role lands after login. */
export const HOME_BY_ROLE: Record<Role, string> = {
  PATIENT: "/dashboard",
  DOCTOR: "/doctor/dashboard",
  ADMIN: "/admin/dashboard",
};

const ROLES: readonly Role[] = ["PATIENT", "DOCTOR", "ADMIN"];

function isRole(value: unknown): value is Role {
  return typeof value === "string" && ROLES.includes(value as Role);
}

/** Home route for a role, with a safe fallback if the backend ever sends an
 *  unexpected value (e.g. an anonymous token leaking through as "ANONYMOUS"). */
export function homeFor(role: unknown): string {
  return isRole(role) ? HOME_BY_ROLE[role] : "/login";
}

/** The Spring login endpoint for each role. */
export const LOGIN_PATH_BY_ROLE: Record<Role, string> = {
  PATIENT: "/patient/login",
  DOCTOR: "/doctor/login",
  ADMIN: "/admin/login",
};

/**
 * Resolve the signed-in user from Spring's SecurityContext.
 * Returns null when unauthenticated — which is a normal outcome, not an error:
 * Spring answers an anonymous request with 302 or 401, both of which mean
 * "nobody is logged in".
 *
 * `use cache: private` is the Next.js 16 idiom for session reads: it may call
 * cookies() and the result stays in the browser, never cached server-side. That
 * keeps the identity lookup off the critical path on repeat navigations.
 */
export async function getCurrentUser(): Promise<CurrentUser | null> {
  "use cache: private";
  try {
    const user = await springJson<CurrentUser>("/api/me");
    // A role we don't recognise means we can't reason about this session —
    // treat it as signed out rather than rendering a broken nav.
    if (!user || !isRole(user.role)) return null;
    return user;
  } catch (err) {
    if (err instanceof ApiError && (err.status === 401 || err.status === 302)) {
      return null;
    }
    // A transport failure (backend down/unreachable) must NOT blank out public
    // pages like the landing page. Treat it as "nobody is signed in" so the
    // marketing surface keeps rendering.
    if (isTransportError(err)) {
      console.warn("[auth] backend unreachable, treating as signed out", err);
      return null;
    }
    // A genuine 500 from /api/me means the endpoint is broken. Rethrow so it
    // surfaces rather than masquerading as a signed-out user.
    throw err;
  }
}

function isTransportError(err: unknown): boolean {
  if (err instanceof ApiError) return false;
  return (
    err instanceof TypeError ||
    (err instanceof Error &&
      /fetch failed|ECONNREFUSED|EAI_AGAIN|socket hang up|terminated/i.test(
        err.message,
      ))
  );
}

/** Guard for pages that require a session. Redirects to login if absent. */
export async function requireUser(returnTo?: string): Promise<CurrentUser> {
  const user = await getCurrentUser();
  if (!user) {
    redirect(returnTo ? `/login?next=${encodeURIComponent(returnTo)}` : "/login");
  }
  return user;
}

/** Guard for role-restricted areas (doctor/admin). */
export async function requireRole(
  allowed: Role[],
  returnTo?: string,
): Promise<CurrentUser> {
  const user = await requireUser(returnTo);
  if (!allowed.includes(user.role)) {
    redirect(homeFor(user.role));
  }
  return user;
}