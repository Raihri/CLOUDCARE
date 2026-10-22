import Link from "next/link";

import { getCurrentUser, homeFor } from "@/lib/auth";

/**
 * The three role entry points. A visitor's first decision is "which door do
 * I go through?" — so the landing page answers it before anything else.
 *
 * hrefs are the signed-OUT destinations. When a session exists we swap in the
 * dashboard instead, so a signed-in admin clicking "Admin" lands on the admin
 * dashboard rather than a login form.
 */
const PORTALS = [
  {
    key: "PATIENT",
    label: "Patient",
    href: "/register",
    icon: "❥",
    description:
      "Donate blood, book doctor appointments and keep your health records in one place.",
  },
  {
    key: "DOCTOR",
    label: "Doctor",
    href: "/doctor/login",
    icon: "⚕",
    description:
      "Manage your availability, review appointments and follow up with your patients.",
  },
  {
    key: "ADMIN",
    label: "Admin",
    href: "/admin/login",
    icon: "◈",
    description:
      "Approve doctors, oversee donors and requests, and keep the platform healthy.",
  },
] as const;

export type PortalKey = (typeof PORTALS)[number]["key"];

/** Destination for a role, honouring an existing session when there is one. */
export function portalHref(key: PortalKey, signedInHref?: string) {
  return signedInHref ?? PORTALS.find((p) => p.key === key)!.href;
}

export function RolePortalCard({
  portal,
  signedInHref,
}: {
  portal: (typeof PORTALS)[number];
  signedInHref?: string;
}) {
  const href = portalHref(portal.key, signedInHref);
  const active = Boolean(signedInHref);

  return (
    <Link
      href={href}
      className={[
        "group flex flex-col gap-3 rounded-lg border bg-surface p-6 shadow-sm",
        "transition-colors hover:border-primary-300 hover:bg-primary-50/40",
        active ? "border-primary-300" : "border-border",
      ].join(" ")}
    >
      <span className="flex size-11 items-center justify-center rounded-xl bg-primary-600 text-xl text-white shadow-sm">
        <span aria-hidden>{portal.icon}</span>
      </span>

      <div>
        <div className="flex items-center gap-2">
          <h3 className="font-semibold text-text-primary">{portal.label}</h3>
          {active ? (
            <span className="rounded-full bg-success-soft px-2 py-0.5 text-xs font-medium text-success">
              Signed in
            </span>
          ) : null}
        </div>
        <p className="mt-1 text-sm text-text-secondary">{portal.description}</p>
      </div>

      <span className="mt-auto text-sm font-medium text-primary-600 group-hover:text-primary-700">
        {active
          ? `Open ${portal.label.toLowerCase()} dashboard →`
          : `Continue as ${portal.label.toLowerCase()} →`}
      </span>
    </Link>
  );
}

/** Server component — reads the session, so callers must wrap it in Suspense. */
export async function RolePortals() {
  const user = await getCurrentUser();
  const signedInHref = user ? homeFor(user.role) : undefined;

  return (
    <section>
      <div className="flex items-baseline justify-between">
        <h2 className="text-lg font-semibold tracking-tight text-text-primary">
          Choose how you&apos;re signing in
        </h2>
        {user ? (
          <p className="text-sm text-text-muted">
            Signed in as {user.name} ({user.role.toLowerCase()})
          </p>
        ) : null}
      </div>

      <div className="mt-4 grid grid-cols-3 gap-4">
        {PORTALS.map((portal) => (
          <RolePortalCard
            key={portal.key}
            portal={portal}
            signedInHref={signedInHref}
          />
        ))}
      </div>
    </section>
  );
}