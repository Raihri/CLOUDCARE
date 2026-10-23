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
import { PatientSearch } from "../patients/PatientSearch";

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

      <Card>
        <CardBody>
          <PatientSearch />
        </CardBody>
      </Card>

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

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
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
                <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
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
                  <div className="divide-y divide-border">
                    {stats.upcoming.map((a) => (
                      <div
                        key={a.id}
                        className="flex flex-col gap-3 py-3 sm:flex-row sm:items-center sm:justify-between"
                      >
                        <div className="min-w-0">
                          <Link
                            href={`/doctor/patients/${a.patientId}`}
                            className="font-medium text-text-primary hover:text-primary-600 hover:underline"
                          >
                            {a.patientName ?? "—"}
                          </Link>
                          <p className="mt-0.5 text-xs text-text-muted">
                            {a.appointmentDate ?? "—"} · {a.timeSlot ?? "—"} ·{" "}
                            {a.type?.toLowerCase() ?? "—"}
                          </p>
                        </div>
                        <div className="flex flex-wrap items-center gap-2">
                          {a.status === "CONFIRMED" && a.telemedicineLink ? (
                            <a
                              href={a.telemedicineLink}
                              target="_blank"
                              rel="noreferrer"
                              className="inline-flex items-center gap-1 rounded-md border border-border-strong bg-surface px-2.5 py-1.5 text-xs font-medium text-primary-700 hover:bg-primary-50"
                            >
                              <i className="bx bx-video text-sm" aria-hidden />
                              Join meeting
                            </a>
                          ) : null}
                          <StatusBadge status={a.status} />
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardBody>
            </Card>
          </div>
        </>
      )}
    </AppShell>
  );
}