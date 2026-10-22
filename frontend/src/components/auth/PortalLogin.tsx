import Link from "next/link";

import { login } from "@/app/login/actions";
import { LoginForm } from "@/app/login/LoginForm";
import type { Role } from "@/lib/auth";
import { AuthCard } from "./AuthCard";

/**
 * Role-specific sign-in shell.
 *
 * One component behind /login, /doctor/login and /admin/login so the three
 * portals stay visually identical apart from their accent and wording. The
 * legacy app had three separate hand-styled login pages.
 */
export function PortalLogin({
  role,
  title,
  subtitle,
  icon,
  footer,
  forgotHref,
}: {
  role: Role;
  title: string;
  subtitle: string;
  icon: string;
  footer?: React.ReactNode;
  /**
   * Where "Forgot password?" points. Each portal has its own reset route and
   * they are not interchangeable — a patient OTP is meaningless to a doctor,
   * whose flow is token-based.
   */
  forgotHref?: string;
}) {
  return (
    <AuthCard
      icon={icon}
      title={title}
      subtitle={subtitle}
      footer={
        <>
          {footer}

          {forgotHref ? (
            <p className="mt-4 text-center text-sm">
              <Link href={forgotHref} className="text-text-muted hover:text-primary-600">
                Forgot your password?
              </Link>
            </p>
          ) : null}

          <p className="mt-4 text-center text-sm text-text-muted">
            <Link
              href="/"
              className="font-medium text-text-secondary hover:text-text-primary"
            >
              ← Back to CloudCare
            </Link>
          </p>
        </>
      }
    >
      <LoginForm action={login} role={role} />
    </AuthCard>
  );
}