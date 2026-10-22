import Link from "next/link";

import { login } from "@/app/login/actions";
import { LoginForm } from "@/app/login/LoginForm";
import type { Role } from "@/lib/auth";

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
}: {
  role: Role;
  title: string;
  subtitle: string;
  icon: string;
  footer?: React.ReactNode;
}) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-surface-sunken px-6 py-12">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center text-center">
          <span className="flex size-11 items-center justify-center rounded-xl bg-primary-600 text-xl text-white">
            <span aria-hidden>{icon}</span>
          </span>
          <h1 className="mt-4 text-xl font-semibold tracking-tight text-text-primary">
            {title}
          </h1>
          <p className="mt-1 text-sm text-text-muted">{subtitle}</p>
        </div>

        <div className="rounded-lg border border-border bg-surface p-6 shadow-sm">
          <LoginForm action={login} role={role} />
        </div>

        {footer}

        <p className="mt-4 text-center text-sm text-text-muted">
          <Link
            href="/"
            className="font-medium text-text-secondary hover:text-text-primary"
          >
            ← Back to CloudCare
          </Link>
        </p>
      </div>
    </div>
  );
}