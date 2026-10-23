import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState, StatCard } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import {
  fetchAdminDashboard,
  fetchAdminDoctors,
  fetchDonorSummary,
  fetchUrgentRequests,
} from "@/lib/admin-api";

export const metadata: Metadata = { title: "Admin Dashboard" };

export const instant = false;

const QUICK_ACTIONS = [
  {
    href: "/admin/doctors/pending",
    icon: "bxs-time",
    label: "Review applications",
    description: "Approve or reject pending doctors",
  },
  {
    href: "/admin/donors",
    icon: "bxs-droplet",
    label: "Donor analysis",
    description: "Supply by blood group and urgent requests",
  },
  {
    href: "/admin/feedbacks",
    icon: "bxs-star",
    label: "Patient feedback",
    description: "Read and moderate submissions",
  },
  {
    href: "/admin/change-password",
    icon: "bxs-lock-alt",
    label: "Change password",
    description: "Update your admin credentials",
  },
] as const;

function initials(name: string | null) {
  if (!name) return "?";
  return name
    .replace(/^Dr\.?\s+/i, "")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}

export default async function AdminDashboardPage() {
  const user = await requireRole(["ADMIN"], "/admin/dashboard");

  // Fetched together so one slow endpoint doesn't delay the rest of the page.
  const [doctors, donors, urgent, pendingDoctors] = await Promise.all([
    fetchAdminDashboard(),
    fetchDonorSummary(),
    fetchUrgentRequests(),
    fetchAdminDoctors("PENDING"),
  ]);

  return (
    <AppShell user={user}>
      <PageHeader
        title="Admin Dashboard"
        description="Platform health at a glance."
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
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

      <div className="grid gap-5 xl:grid-cols-3">
        <Card className="xl:col-span-2">
          <CardHeader
            title="Pending applications"
            description="New doctors waiting for a decision."
            action={
              <Link
                href="/admin/doctors/pending"
                className="text-sm font-medium text-highlight hover:underline"
              >
                Review all →
              </Link>
            }
          />
          <CardBody>
            {pendingDoctors.length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-check-circle" aria-hidden />}
                title="All caught up"
                description="No doctor applications are waiting for review."
              />
            ) : (
              <ul className="divide-y divide-border">
                {pendingDoctors.slice(0, 4).map((doctor) => (
                  <li key={doctor.id} className="flex items-center gap-3 py-3">
                    <span className="flex size-9 shrink-0 items-center justify-center rounded-full bg-primary-50 text-xs font-semibold text-primary-700">
                      {initials(doctor.name)}
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium text-text-primary">
                        {doctor.name ?? "Unnamed applicant"}
                      </p>
                      <p className="truncate text-xs text-text-muted">
                        {doctor.specialization ?? "Specialization not set"}
                        {doctor.bmdcRegNo ? ` · ${doctor.bmdcRegNo}` : ""}
                      </p>
                    </div>
                    <Link
                      href="/admin/doctors/pending"
                      className="shrink-0 rounded-md border border-border-strong bg-surface px-2.5 py-1.5 text-xs font-medium text-text-primary transition-colors hover:bg-neutral-50"
                    >
                      Review
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader
            title="Quick actions"
            description="The work that usually needs you."
          />
          <CardBody className="pt-1">
            <ul className="divide-y divide-border">
              {QUICK_ACTIONS.map((action) => (
                <li key={action.href}>
                  <Link
                    href={action.href}
                    className="group flex items-center gap-3 py-3 transition-colors"
                  >
                    <span className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-700">
                      <i
                        aria-hidden
                        className={`bx ${action.icon} text-lg leading-none`}
                      />
                    </span>
                    <span className="min-w-0 flex-1">
                      <span className="block text-sm font-medium text-text-primary group-hover:text-primary-700">
                        {action.label}
                      </span>
                      <span className="block truncate text-xs text-text-muted">
                        {action.description}
                      </span>
                    </span>
                    <i
                      aria-hidden
                      className="bx bx-chevron-right text-lg text-text-muted transition-transform group-hover:translate-x-0.5 group-hover:text-primary-600"
                    />
                  </Link>
                </li>
              ))}
            </ul>
          </CardBody>
        </Card>
      </div>

      <div className="grid gap-5 xl:grid-cols-2">
        <Card>
          <CardHeader
            title="Urgent blood requests"
            description="Pending requests due within three days."
            action={
              <Link
                href="/admin/donors"
                className="text-sm font-medium text-highlight hover:underline"
              >
                Donor analysis →
              </Link>
            }
          />
          <CardBody>
            {urgent.length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-droplet" aria-hidden />}
                title="Nothing urgent"
                description="No pending requests due within three days."
              />
            ) : (
              <ul className="divide-y divide-border">
                {urgent.slice(0, 6).map((request) => (
                  <li
                    key={request.id}
                    className="flex items-center justify-between gap-3 py-3"
                  >
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-text-primary">
                        {request.name ?? "Anonymous"}
                      </p>
                      <p className="text-xs text-text-muted">
                        {request.bloodGroup ?? "—"} · {request.units ?? "?"}{" "}
                        unit{request.units === 1 ? "" : "s"}
                      </p>
                    </div>
                    <span className="shrink-0 text-xs font-medium text-danger">
                      {request.requiredDate ?? "No date"}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader
            title="Donors by blood group"
            description="Registered donor supply across every group."
          />
          <CardBody>
            {!donors || Object.keys(donors.byBloodGroup).length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-bar-chart-alt-2" aria-hidden />}
                title="No donors yet"
                description="Blood group breakdown appears once donors register."
              />
            ) : (
              <ul className="space-y-3">
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
