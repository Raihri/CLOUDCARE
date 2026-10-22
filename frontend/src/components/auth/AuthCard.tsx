import type { ReactNode } from "react";

/**
 * The shell every signed-out page shares: centred card on the sunken
 * background, with the CloudCare mark and a title block above it.
 *
 * <p>PortalLogin and the three password-reset pages all render through this.
 * It was duplicated by hand four times before, which is precisely how the next
 * edit ends up applying to one page and silently missing the other three.
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
    <div className="flex min-h-screen items-center justify-center bg-surface-sunken px-6 py-12">
      <div className={wide ? "w-full max-w-lg" : "w-full max-w-sm"}>
        <div className="mb-6 flex flex-col items-center text-center">
          <span className="flex size-11 items-center justify-center rounded-xl bg-primary-600 text-xl text-white">
            <span aria-hidden>{icon}</span>
          </span>
          <h1 className="mt-4 text-xl font-semibold tracking-tight text-text-primary">
            {title}
          </h1>
          {subtitle ? <p className="mt-1 text-sm text-text-muted">{subtitle}</p> : null}
        </div>

        <div className="rounded-lg border border-border bg-surface p-6 shadow-sm">
          {children}
        </div>

        {footer}
      </div>
    </div>
  );
}