import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState, StatCard } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import {
  fetchAdminDashboard,
  fetchDonorSummary,
  fetchUrgentRequests,
} from "@/lib/admin-api";

export const metadata: Metadata = { title: "Admin Dashboard" };

export const instant = false;

export default async function AdminDashboardPage() {
  const user = await requireRole(["ADMIN"], "/admin/dashboard");

  // Fetched together so one slow endpoint doesn't delay the rest of the page.
  const [doctors, donors, urgent] = await Promise.all([
    fetchAdminDashboard(),
    fetchDonorSummary(),
    fetchUrgentRequests(),
  ]);

  return (
    <AppShell user={user}>
      <PageHeader
        title="Admin Dashboard"
        description="Platform health at a glance."
      />

      <div className="grid grid-cols-4 gap-4">
        <StatCard
          label="Doctors"
          value={doctors?.totalDoctors ?? "—"}
          icon={<i className="bx bxs-user" aria-hidden />}
        />
        <StatCard
          label="Pending approval"
          value={doctors?.pendingDoctors ?? "—"}
          hint="Awaiting review"
          icon={<i className="bx bxs-time" aria-hidden />}
        />
        <StatCard
          label="Approved doctors"
          value={doctors?.approvedDoctors ?? "—"}
          icon={<i className="bx bxs-check-circle" aria-hidden />}
        />
        <StatCard
          label="Registered donors"
          value={donors?.totalDonors ?? "—"}
          hint={`${donors?.eligibleDonors ?? 0} currently eligible`}
          icon={<i className="bx bxs-droplet" aria-hidden />}
        />
      </div>

      <div className="mt-5 grid grid-cols-2 gap-5">
        <Card>
          <CardBody>
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-base font-semibold text-text-primary">
                Urgent blood requests
              </h2>
              <Link
                href="/admin/donors"
                className="text-sm font-medium text-highlight hover:underline"
              >
                Donor analysis →
              </Link>
            </div>

            {urgent.length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-droplet" aria-hidden />}
                title="Nothing urgent"
                description="No pending requests due within three days."
              />
            ) : (
              <ul className="divide-y divide-border">
                {urgent.slice(0, 6).map((r) => (
                  <li key={r.id} className="flex items-center justify-between py-3">
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-text-primary">
                        {r.name ?? "Anonymous"}
                      </p>
                      <p className="text-xs text-text-muted">
                        {r.bloodGroup ?? "—"} · {r.units ?? "?"} unit
                        {r.units === 1 ? "" : "s"}
                      </p>
                    </div>
                    <span className="ml-3 shrink-0 text-xs font-medium text-danger">
                      {r.requiredDate ?? "No date"}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardBody>
            <h2 className="mb-4 text-base font-semibold text-text-primary">
              Donors by blood group
            </h2>

            {!donors || Object.keys(donors.byBloodGroup).length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-bar-chart-alt-2" aria-hidden />}
                title="No donors yet"
                description="Blood group breakdown appears once donors register."
              />
            ) : (
              <ul className="space-y-2">
                {Object.entries(donors.byBloodGroup)
                  .sort(([, a], [, b]) => b - a)
                  .map(([group, count]) => {
                    // Scale bars against the largest group so the comparison is
                    // readable regardless of absolute numbers.
                    const max = Math.max(
                      ...Object.values(donors.byBloodGroup),
                    );
                    return (
                      <li key={group} className="flex items-center gap-3">
                        <span className="w-10 shrink-0 text-sm font-medium text-text-primary">
                          {group}
                        </span>
                        <div className="h-2 flex-1 overflow-hidden rounded-full bg-neutral-200">
                          <div
                            className="h-full rounded-full bg-blood"
                            style={{ width: `${(count / max) * 100}%` }}
                          />
                        </div>
                        <span className="w-8 shrink-0 text-right text-sm text-text-secondary">
                          {count}
                        </span>
                      </li>
                    );
                  })}
              </ul>
            )}
          </CardBody>
        </Card>
      </div>
    </AppShell>
  );
}