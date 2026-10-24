import type { Metadata } from "next";
import Link from "next/link";

import type { HealthRecord } from "@/lib/health-record";
import { AppShell } from "@/components/layout/AppShell";
import { ProfileCard } from "@/components/patient/ProfileCard";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState, StatCard } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { StatusBadge } from "@/components/ui/Badge";
import { springJson } from "@/lib/api";
import { requireRole } from "@/lib/auth";
import {
  fetchAppointments,
  fetchDashboard,
  fetchMyRequests,
  fetchPrescriptions,
} from "@/lib/patient-api";

export const metadata: Metadata = { title: "Patient Profile" };

// Authenticated, per-user data — not prerenderable.
export const instant = false;

export default async function DashboardPage() {
  const user = await requireRole(["PATIENT"], "/dashboard");

  // All three fetches tolerate failure internally, so a backend blip degrades
  // this page to empty panels rather than a 500.
  const [stats, requests, appointments, prescriptions] = await Promise.all([
    fetchDashboard(),
    fetchMyRequests(),
    fetchAppointments(),
    fetchPrescriptions(),
  ]);

  // The profile card is the centrepiece, but it must never take the page down:
  // a failure here just omits the card and leaves the panels below.
  let record: HealthRecord | null = null;
  try {
    record = await springJson<HealthRecord>("/patient/api/health");
  } catch {
    record = null;
  }

  const recentRequests = requests.slice(0, 5);
  const upcoming = appointments
    .filter((a) => a.status !== "CANCELLED")
    .slice(0, 5);

  const firstName = user.name?.split(" ")[0] ?? user.email;

  return (
    <AppShell
      user={user}
      counts={{
        // The dashboard already fetches this, so the sidebar bell can show the
        // unread count from here without a second request.
        unreadNotifications: stats?.unreadNotifications ?? 0,
      }}
    >
      {record ? <ProfileCard record={record} /> : null}

      {!record ? (
        <PageHeader
          title={`Welcome back, ${firstName}`}
          description="Here's what's happening with your health today."
        />
      ) : (
        <div className="-mt-3">
          <Link
            href="/health-metrics"
            className="inline-flex items-center gap-2 text-sm font-medium text-highlight hover:underline"
          >
            <i className="bx bxs-heart" aria-hidden />
            View your health metrics
          </Link>
        </div>
      )}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label="My blood requests"
          value={stats?.myRequests ?? 0}
          icon={<i className="bx bxs-droplet" aria-hidden />}
        />
        <StatCard
          label="Awaiting a match"
          value={stats?.pendingRequests ?? 0}
          icon={<i className="bx bxs-time" aria-hidden />}
        />
        <StatCard
          label="Appointments"
          value={stats?.appointments ?? 0}
          icon={<i className="bx bxs-calendar-check" aria-hidden />}
        />
        <StatCard
          label="Unread notifications"
          value={stats?.unreadNotifications ?? 0}
          icon={<i className="bx bxs-bell" aria-hidden />}
        />
        <StatCard
          label="Prescriptions"
          value={stats?.prescriptions ?? 0}
          icon={<i className="bx bxs-capsule" aria-hidden />}
        />
        <StatCard
          label="Upcoming follow-ups"
          value={stats?.upcomingFollowUps ?? 0}
          hint="Visits your doctor asked you to plan"
          icon={<i className="bx bxs-calendar" aria-hidden />}
        />
      </div>

      <Card>
        <CardHeader
          title="Prescriptions & follow-ups"
          description="Medicines and next visits your doctors have planned."
          action={
            <Link
              href="/prescriptions"
              className="text-sm font-medium text-highlight hover:underline"
            >
              View all
            </Link>
          }
        />
        <CardBody>
          {prescriptions.length === 0 ? (
            <EmptyState
              icon={<i className="bx bxs-capsule" aria-hidden />}
              title="No prescriptions yet"
              description="A doctor's prescription and follow-up date will appear here."
            />
          ) : (
            <ul className="flex flex-col divide-y divide-border">
              {prescriptions.slice(0, 3).map((p) => (
                <li
                  key={p.id}
                  className="flex flex-col gap-1 py-3 first:pt-0 last:pb-0 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium text-text-primary">
                      {p.doctorName ?? "Your doctor"}
                    </p>
                    <p className="truncate text-xs text-text-muted">
                      {p.medicines.split("\n")[0]}
                    </p>
                  </div>
                  <span
                    className={[
                      "shrink-0 rounded-full px-2.5 py-0.5 text-xs font-medium",
                      p.followUpStatus === "DUE_TODAY" ||
                      p.followUpStatus === "OVERDUE"
                        ? "bg-danger-soft text-danger"
                        : p.followUpStatus === "UPCOMING"
                          ? "bg-warning-soft text-warning"
                          : "bg-neutral-100 text-text-secondary",
                    ].join(" ")}
                  >
                    {p.followUpDate
                      ? p.followUpStatus === "DUE_TODAY"
                        ? "Follow-up today"
                        : `${p.followUpDate}`
                      : "No follow-up"}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </CardBody>
      </Card>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        {/* ---- Recent requests ---- */}
        <Card>
          <CardHeader
            title="My recent requests"
            description="Blood requests you've posted."
            action={
              <Link
                href="/requests"
                className="text-sm font-medium text-highlight hover:underline"
              >
                View all
              </Link>
            }
          />
          <CardBody>
            {recentRequests.length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-droplet" aria-hidden />}
                title="No blood requests yet"
                description="Post a request and we'll match you with eligible donors."
              />
            ) : (
              <ul className="flex flex-col divide-y divide-border">
                {recentRequests.map((r) => (
                  <li
                    key={r.id}
                    className="flex items-center justify-between gap-3 py-3 first:pt-0 last:pb-0"
                  >
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-text-primary">
                        {r.units} unit{r.units === 1 ? "" : "s"} of{" "}
                        <span className="text-blood">{r.bloodGroup}</span>
                      </p>
                      <p className="truncate text-xs text-text-muted">
                        {[r.district, r.thana].filter(Boolean).join(", ") ||
                          "Location not set"}
                      </p>
                    </div>
                    <StatusBadge status={r.status} />
                  </li>
                ))}
              </ul>
            )}
          </CardBody>
        </Card>

        {/* ---- Upcoming appointments ---- */}
        <Card>
          <CardHeader
            title="Upcoming appointments"
            action={
              <Link
                href="/appointments"
                className="text-sm font-medium text-highlight hover:underline"
              >
                View all
              </Link>
            }
          />
          <CardBody>
            {upcoming.length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-calendar-check" aria-hidden />}
                title="No appointments booked"
                description="Find a doctor and pick a time slot."
                action={
                  <Link
                    href="/doctors"
                    className="rounded-md bg-primary-900 px-4 py-2 text-sm font-medium text-white hover:bg-primary-800"
                  >
                    Browse doctors
                  </Link>
                }
              />
            ) : (
              <ul className="flex flex-col divide-y divide-border">
                {upcoming.map((a) => (
                  <li
                    key={a.id}
                    className="flex items-center justify-between gap-3 py-3 first:pt-0 last:pb-0"
                  >
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-text-primary">
                        {a.doctorName}
                      </p>
                      <p className="truncate text-xs text-text-muted">
                        {a.appointmentDate} · {a.timeSlot}
                      </p>
                    </div>
                    <div className="flex shrink-0 items-center gap-2">
                      {a.status === "CONFIRMED" && a.telemedicineLink ? (
                        <a
                          href={a.telemedicineLink}
                          target="_blank"
                          rel="noreferrer"
                          className="inline-flex items-center gap-1 rounded-md border border-border-strong bg-surface px-2.5 py-1.5 text-xs font-medium text-primary-700 hover:bg-primary-50"
                        >
                          <i className="bx bx-video text-sm" aria-hidden />
                          Join
                        </a>
                      ) : null}
                      <StatusBadge status={a.status} />
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </CardBody>
        </Card>
      </div>

      {/* ---- Quick actions ---- */}
      <Card>
        <CardHeader title="Quick actions" />
        <CardBody className="flex flex-wrap gap-3">
          {[
            { href: "/requests/new", label: "Post a blood request", icon: "bxs-droplet" },
            { href: "/donors", label: "Find a donor", icon: "bx-search-alt" },
            { href: "/doctors", label: "Book an appointment", icon: "bxs-calendar-check" },
            { href: "/donors/new", label: "Register as a donor", icon: "bxs-heart" },
          ].map((action) => (
            <Link
              key={action.href}
              href={action.href}
              className="flex items-center gap-2 rounded-md border border-border px-4 py-2.5 text-sm text-text-primary transition-colors hover:border-primary-600 hover:bg-primary-50"
            >
              <i className={`bx ${action.icon} text-lg text-highlight`} aria-hidden />
              {action.label}
            </Link>
          ))}
        </CardBody>
      </Card>
    </AppShell>
  );
}