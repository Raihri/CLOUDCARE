import Link from "next/link";

import { getCurrentUser, homeFor } from "@/lib/auth";

/**
 * The three role entry points for the portal hub at `/`.
 *
 * DESIGN NOTE — why these are rows and not cards.
 *
 * The previous version rendered three tall bordered cards, each with an accent
 * bar, a 48px icon tile, an eyebrow, a heading, a paragraph, three checkmark
 * bullets and a button. Beside a separate hero and a separate three-card
 * "What's inside" grid, that produced seven headings and six boxes on a single
 * screen — which is exactly the "everything shouts, nothing lands" look.
 *
 * Now each portal is one full-width row: role name, one line of prose, arrow.
 * The rows are separated by hairlines instead of boxes, so the page has one
 * focal point (the list) and the eye can scan down it. Icon tiles are gone —
 * three icons in a column add colour but no information the words don't carry.
 *
 * Every class below is a complete literal so the Tailwind compiler sees it.
 */
const PORTALS = [
  {
    key: "PATIENT",
    label: "Patient",
    blurb: "Appointments, blood requests and health records.",
    href: "/login?next=%2Fdashboard",
    secondary: { label: "Create an account", href: "/register" },
  },
  {
    key: "DOCTOR",
    label: "Doctor",
    blurb: "Availability, appointments and your patient list.",
    href: "/doctor/login",
    secondary: { label: "Apply as a doctor", href: "/doctor/signup" },
  },
  {
    key: "ADMIN",
    label: "Admin",
    blurb: "Doctor approvals, donors and feedback.",
    href: "/admin/login",
    secondary: null,
  },
] as const;

export type PortalKey = (typeof PORTALS)[number]["key"];

/** Server component — reads the session, so callers must wrap it in Suspense. */
export async function RolePortals() {
  const user = await getCurrentUser();

  return (
    <ul className="border-t border-border">
      {PORTALS.map((portal) => {
        const isCurrentRole = user?.role === portal.key;
        const href = isCurrentRole ? homeFor(user.role) : portal.href;

        return (
          <li key={portal.key} className="border-b border-border">
            <div className="group flex flex-col gap-4 py-7 sm:flex-row sm:items-center sm:gap-8">
              {/* Role name. The one place the page is allowed to be loud, so
                  the three portals read as a ranked list rather than prose. */}
              <h2 className="flex w-full shrink-0 items-center gap-3 text-2xl font-semibold tracking-tight text-text-primary sm:w-40">
                {portal.label}
                {isCurrentRole ? (
                  <span className="inline-flex items-center gap-1 rounded-full bg-success-soft px-2 py-0.5 text-[11px] font-semibold text-success">
                    <i
                      aria-hidden
                      className="bx bxs-check-circle text-xs leading-none"
                    />
                    You
                  </span>
                ) : null}
              </h2>

              <p className="min-w-0 flex-1 text-sm leading-relaxed text-text-secondary">
                {portal.blurb}
              </p>

              {/* Both actions are real buttons of equal weight: the primary is
                  filled, the secondary is a bordered sibling. A ghost text link
                  was tried here and read as an afterthought next to a filled
                  button, so both now sit on the same 44px row.

                  `sm:justify-end` would right-align a lone button against a
                  ragged edge, so the whole group is pinned to a fixed width and
                  left-aligned instead — the three Sign in buttons then line up
                  in a column whether or not a secondary sits beside them. */}
              <div className="flex w-full shrink-0 flex-wrap items-center gap-2 sm:w-[22rem]">
                <Link
                  href={href}
                  className="inline-flex h-11 items-center justify-center gap-2 rounded-lg bg-primary-900 px-5 text-sm font-medium text-white transition-colors hover:bg-primary-800"
                >
                  {isCurrentRole ? "Open dashboard" : "Sign in"}
                  <i
                    aria-hidden
                    className="bx bx-right-arrow-alt text-lg leading-none"
                  />
                </Link>

                {isCurrentRole ? (
                  // A form, not a Link: /api/logout is POST-only, so a GET
                  // navigation would render the endpoint's response instead of
                  // signing anyone out.
                  <form action="/api/logout" method="post">
                    <button
                      type="submit"
                      className="inline-flex h-11 items-center justify-center rounded-lg px-4 text-sm font-medium text-text-muted transition-colors hover:bg-neutral-100 hover:text-text-primary"
                    >
                      Sign out
                    </button>
                  </form>
                ) : portal.secondary ? (
                  <Link
                    href={portal.secondary.href}
                    className="inline-flex h-11 items-center justify-center rounded-lg border border-border px-4 text-sm font-medium text-text-primary transition-colors hover:border-primary-300 hover:bg-primary-50"
                  >
                    {portal.secondary.label}
                  </Link>
                ) : null}
              </div>
            </div>
          </li>
        );
      })}
    </ul>
  );
}