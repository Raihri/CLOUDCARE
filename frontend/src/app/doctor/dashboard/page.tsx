import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { StatusBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { StatCard } from "@/components/ui/Feedback";
import { requireRole } from "@/lib/auth";
import { fetchDoctorDashboard } from "@/lib/doctor-api";

export const metadata: Metadata = { title: "Doctor Dashboard" };

export const instant = false;

export default async function DoctorDashboardPage() {
  const user = await requireRole(["DOCTOR"], "/doctor/dashboard");
  const stats = await fetchDoctorDashboard();

  return (
    <AppShell user={user}>
      <PageHeader
        title={`Welcome, ${stats?.name ?? user.name ?? "Doctor"}`}
        description="Your appointments and availability at a glance."
      />

      {!stats ? (
        <Card>
          <CardBody>
            <EmptyState
              icon={<i className="bx bxs-error" aria-hidden />}
              title="Couldn't load your dashboard"
              description="Please refresh and try again."
            />
          </CardBody>
        </Card>
      ) : (
        <>
          {stats.status && stats.status !== "APPROVED" ? (
            <div className="mb-5 rounded-md border border-warning/40 bg-warning-soft px-4 py-3 text-sm text-text-primary">
              Your account is{" "}
              <strong className="capitalize">
                {stats.status.toLowerCase().replace("_", " ")}
              </strong>
              . Patients can see your profile once an admin approves it.
            </div>
          ) : null}

          <div className="grid grid-cols-4 gap-4">
            <StatCard
              label="Total appointments"
              value={stats.totalAppointments}
              icon={<i className="bx bxs-calendar-check" aria-hidden />}
            />
            <StatCard
              label="Awaiting confirmation"
              value={stats.pendingAppointments}
              hint="Confirm or cancel these"
              icon={<i className="bx bxs-time" aria-hidden />}
            />
            <StatCard
              label="Confirmed"
              value={stats.confirmedAppointments}
              icon={<i className="bx bxs-check-circle" aria-hidden />}
            />
            <StatCard
              label="Open slots"
              value={stats.openSlots}
              hint="Available for booking"
              icon={<i className="bx bxs-clock" aria-hidden />}
            />
          </div>

          <div className="mt-5">
            <Card>
              <CardBody>
                <div className="mb-4 flex items-center justify-between">
                  <h2 className="text-base font-semibold text-text-primary">
                    Today and upcoming
                  </h2>
                  <Link
                    href="/doctor/appointments"
                    className="text-sm font-medium text-highlight hover:underline"
                  >
                    All appointments →
                  </Link>
                </div>

                {stats.upcoming.length === 0 ? (
                  <EmptyState
                    icon={<i className="bx bxs-calendar" aria-hidden />}
                    title="Nothing scheduled"
                    description="New bookings will appear here."
                  />
                ) : (
                  <table className="w-full text-left text-sm">
                    <thead>
                      <tr className="border-b border-border text-xs uppercase tracking-wide text-text-muted">
                        <th className="pb-3 pr-4 font-medium">Patient</th>
                        <th className="pb-3 pr-4 font-medium">Date</th>
                        <th className="pb-3 pr-4 font-medium">Time</th>
                        <th className="pb-3 pr-4 font-medium">Type</th>
                        <th className="pb-3 font-medium">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-border">
                      {stats.upcoming.map((a) => (
                        <tr key={a.id}>
                          <td className="py-3 pr-4 font-medium text-text-primary">
                            {a.patientName ?? "—"}
                          </td>
                          <td className="py-3 pr-4 text-text-secondary">
                            {a.appointmentDate ?? "—"}
                          </td>
                          <td className="py-3 pr-4 text-text-secondary">
                            {a.timeSlot ?? "—"}
                          </td>
                          <td className="py-3 pr-4 text-text-secondary">
                            {a.type?.toLowerCase() ?? "—"}
                          </td>
                          <td className="py-3">
                            <StatusBadge status={a.status} />
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </CardBody>
            </Card>
          </div>
        </>
      )}
    </AppShell>
  );
}