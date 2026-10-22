import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState, StatCard } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { StatusBadge } from "@/components/ui/Badge";
import { requireRole } from "@/lib/auth";
import {
  fetchAppointments,
  fetchDashboard,
  fetchMyRequests,
} from "@/lib/patient-api";

export const metadata: Metadata = { title: "Dashboard" };

// Authenticated, per-user data — not prerenderable.
export const instant = false;

export default async function DashboardPage() {
  const user = await requireRole(["PATIENT"], "/dashboard");

  // All three fetches tolerate failure internally, so a backend blip degrades
  // this page to empty panels rather than a 500.
  const [stats, requests, appointments] = await Promise.all([
    fetchDashboard(),
    fetchMyRequests(),
    fetchAppointments(),
  ]);

  const recentRequests = requests.slice(0, 5);
  const upcoming = appointments
    .filter((a) => a.status !== "CANCELLED")
    .slice(0, 5);

  const firstName = user.name?.split(" ")[0] ?? user.email;

  return (
    <AppShell user={user}>
      <PageHeader
        title={`Welcome back, ${firstName}`}
        description="Here's what's happening with your health today."
      />

      <div className="grid grid-cols-4 gap-4">
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
      </div>

      <div className="grid grid-cols-2 gap-4">
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
                    <StatusBadge status={a.status} />
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