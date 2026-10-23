import type { Role } from "@/lib/auth";

import { AuthExperience } from "./AuthExperience";

/**
 * Role-specific sign-in shell.
 *
 * One wrapper behind /doctor/login and /admin/login so the portals stay
 * visually identical apart from their copy, demo shortcut and illustration.
 * The legacy app had two separate hand-styled login pages.
 */
export function PortalLogin({
  role,
  next,
  forgotHref,
  /**
   * Where clinicians can start an application. Admin has no self-service
   * route, so that page simply omits it.
   */
  signupHref,
}: {
  role: Role;
  next?: string;
  /**
   * Where "Forgot password?" points. Each portal has its own reset route and
   * they are not interchangeable — a patient OTP is meaningless to a doctor,
   * whose flow is token-based.
   */
  forgotHref?: string;
  signupHref?: string;
}) {
  return (
    <AuthExperience
      initialMode="login"
      role={role}
      next={next}
      forgotHref={forgotHref}
      signupHref={signupHref}
    />
  );
}
