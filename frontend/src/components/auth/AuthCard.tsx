import type { ReactNode } from "react";

import { BrandLogo } from "@/components/brand/BrandLogo";

/**
 * The shell every signed-out page shares: centred card on the sunken
 * background, with the CloudCare mark and a title block above it.
 *
 * <p>The password-reset pages and the doctor application form all render
 * through this. Portal logins have their own dual-panel shell
 * (AuthExperience), but signed-out utility pages keep the simpler centred card
 * so a reset link feels like a quiet detour rather than a second landing page.
 */
export function AuthCard({
  icon,
  title,
  subtitle,
  children,
  footer,
  wide = false,
}: {
  icon: ReactNode;
  title: string;
  subtitle?: ReactNode;
  children: ReactNode;
  /** Links rendered under the card — "Forgot password?", "Back to CloudCare". */
  footer?: ReactNode;
  /** Wider card for forms with many fields, e.g. doctor signup. */
  wide?: boolean;
}) {
  return (
    <div className="flex min-h-screen justify-center bg-surface-sunken px-4 py-6 sm:py-10">
      {/* `my-auto` centres short forms without clipping tall ones: when the card
          is taller than the viewport the auto margin resolves to 0 and the
          page scrolls from the very top. `items-center` clipped the top of the
          doctor application form on laptop-height screens. */}
      <div
        className={`my-auto w-full ${wide ? "max-w-3xl" : "max-w-sm"}`}
      >
        <div className="mb-6 flex flex-col items-center text-center">
          <div className="relative">
            <BrandLogo
              size={96}
              priority
              className="shadow-md ring-1 ring-border"
            />
            {/* Keep the page-specific cue (lock, first-aid, heart) as a small
                badge so the logo is the brand anchor but password resets and
                doctor applications still say what they are at a glance. */}
            <span className="absolute -top-1.5 -right-1.5 flex size-7 items-center justify-center rounded-full bg-primary-600 text-white shadow-sm ring-2 ring-surface">
              <span aria-hidden>{icon}</span>
            </span>
          </div>
          <h1 className="mt-4 text-xl font-semibold tracking-tight text-text-primary">
            {title}
          </h1>
          {subtitle ? <p className="mt-1 text-sm text-text-muted">{subtitle}</p> : null}
        </div>

        <div className="rounded-xl border border-border bg-surface p-5 shadow-md sm:p-6">
          {children}
        </div>

        {footer}
      </div>
    </div>
  );
}