import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { BloodGroupBadge, StatusBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import {
  fetchDonors,
  fetchMyRequests,
} from "@/lib/patient-api";

export const metadata: Metadata = { title: "Donor & Request Dashboard" };

export const instant = false;

/**
 * Ported from templates/donor_dashboard.html, which is a single page combining
 * a donors registry, a blood-requests list and a medical-reason breakdown.
 *
 * Filtering is done by URL query params rather than client-side JS so the
 * filtered view is shareable and server-rendered — matching how the rest of
 * the app handles list filters (see the admin feedback page).
 */
export default async function DonorDashboardPage({
  searchParams,
}: {
  searchParams: Promise<{ blood?: string; status?: string }>;
}) {
  const user = await requireRole(["PATIENT"], "/donor-dashboard");
  const { blood, status } = await searchParams;

  // Requests come from the patient's own list and donors from the public
  // registry; both degrade to [] on failure so one dead panel doesn't 500.
  const [allDonors, myRequests] = await Promise.all([
    fetchDonors(),
    fetchMyRequests(),
  ]);

  const donors = blood
    ? allDonors.filter(
        (d) => (d.bloodGroup ?? "").toUpperCase() === blood.toUpperCase(),
      )
    : allDonors;

  const requests = status
    ? myRequests.filter(
        (r) => (r.status ?? "").toUpperCase() === status.toUpperCase(),
      )
    : myRequests;

  // Groups are derived from what's actually registered rather than a fixed
  // list, so a group with no donors doesn't clutter the filter.
  const groups = Array.from(
    new Set(
      allDonors
        .map((d) => d.bloodGroup)
        .filter((g): g is string => Boolean(g))
        .map((g) => g.toUpperCase()),
    ),
  ).sort();

  return (
    <AppShell user={user}>
      <PageHeader
        title="Blood Donor & Request Dashboard"
        description="Registered donors and your requests, side by side."
        action={
          <a
            href="/donors/new"
            className="rounded-md bg-blood px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blood-dark"
          >
            Register as a donor
          </a>
        }
      />

      <div className="flex flex-wrap items-center gap-3">
        <span className="text-sm text-text-muted">Filter:</span>

        <a
          href="/donor-dashboard"
          className={
            !blood && !status
              ? "rounded-md bg-navy px-3 py-1.5 text-sm font-medium text-white"
              : "rounded-md border border-border-strong bg-surface px-3 py-1.5 text-sm font-medium text-text-primary hover:bg-neutral-50"
          }
        >
          All
        </a>

        {groups.map((g) => (
          <a
            key={g}
            href={`/donor-dashboard?blood=${g}`}
            className={
              blood === g
                ? "rounded-md bg-blood px-3 py-1.5 text-sm font-medium text-white"
                : "rounded-md border border-border-strong bg-surface px-3 py-1.5 text-sm font-medium text-text-primary hover:bg-neutral-50"
            }
          >
            {g}
          </a>
        ))}

        {["PENDING", "MATCHED"].map((s) => (
          <a
            key={s}
            href={`/donor-dashboard?status=${s}`}
            className={
              status === s
                ? "rounded-md bg-navy px-3 py-1.5 text-sm font-medium text-white"
                : "rounded-md border border-border-strong bg-surface px-3 py-1.5 text-sm font-medium text-text-primary hover:bg-neutral-50"
            }
          >
            {s.charAt(0) + s.slice(1).toLowerCase()}
          </a>
        ))}
      </div>

      <div className="mt-5 grid grid-cols-2 gap-5">
        {/* ---------------- Donors registry ---------------- */}
        <Card>
          <CardBody>
            <h2 className="mb-4 flex items-center gap-2 text-base font-semibold text-text-primary">
              <i className="bx bxs-droplet text-blood" aria-hidden />
              Blood Donors Registry
            </h2>

            {donors.length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-droplet" aria-hidden />}
                title="No donors match"
                description={
                  blood
                    ? `No registered donors for ${blood}.`
                    : "No donors have registered yet."
                }
              />
            ) : (
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-border text-xs uppercase tracking-wide text-text-muted">
                    <th className="pb-3 pr-4 font-medium">Name</th>
                    <th className="pb-3 pr-4 font-medium">Group</th>
                    <th className="pb-3 pr-4 font-medium">Location</th>
                    <th className="pb-3 font-medium">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {donors.map((d) => (
                    <tr key={d.id}>
                      <td className="py-3 pr-4 font-medium text-text-primary">
                        {d.name ?? "—"}
                      </td>
                      <td className="py-3 pr-4">
                        <BloodGroupBadge group={d.bloodGroup} />
                      </td>
                      <td className="py-3 pr-4 text-text-secondary">
                        {[d.district, d.thana].filter(Boolean).join(", ") ||
                          d.location ||
                          "—"}
                      </td>
                      <td className="py-3">
                        <StatusBadge status={d.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </CardBody>
        </Card>

        {/* ---------------- Blood requests ---------------- */}
        <Card>
          <CardBody>
            <h2 className="mb-4 flex items-center gap-2 text-base font-semibold text-text-primary">
              <i className="bx bxs-file-text text-highlight" aria-hidden />
              Blood Requests
            </h2>

            {requests.length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-file-text" aria-hidden />}
                title="No requests"
                description={
                  status
                    ? `You have no ${status.toLowerCase()} requests.`
                    : "You haven't posted a request yet."
                }
                action={
                  <a
                    href="/requests/new"
                    className="rounded-md bg-primary-900 px-4 py-2 text-sm font-medium text-white hover:bg-primary-800"
                  >
                    Post a request
                  </a>
                }
              />
            ) : (
              <ul className="divide-y divide-border">
                {requests.map((r) => (
                  <li key={r.id} className="py-3">
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <p className="flex items-center gap-2 text-sm font-medium text-text-primary">
                          <BloodGroupBadge group={r.bloodGroup} />
                          {r.units} unit{r.units === 1 ? "" : "s"}
                        </p>
                        <p className="mt-1 text-xs text-text-muted">
                          {[r.district, r.thana].filter(Boolean).join(", ") ||
                            r.location ||
                            "—"}
                          {r.requiredDate ? ` · needed by ${r.requiredDate}` : ""}
                        </p>
                        {r.medicalReason ? (
                          <p className="mt-1 truncate text-xs text-text-secondary">
                            {r.medicalReason}
                          </p>
                        ) : null}
                      </div>
                      <StatusBadge status={r.status} />
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </CardBody>
        </Card>
      </div>
    </AppShell>
  );
}