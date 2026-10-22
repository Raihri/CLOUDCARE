import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { StatusBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchAppointments } from "@/lib/patient-api";

export const metadata: Metadata = { title: "Appointments" };

export const instant = false;

export default async function AppointmentsPage() {
  const user = await requireRole(["PATIENT"], "/appointments");
  const appointments = await fetchAppointments();

  const active = appointments.filter((a) => a.status !== "CANCELLED");
  const past = appointments.filter((a) => a.status === "CANCELLED");

  return (
    <AppShell user={user}>
      <PageHeader
        title="Appointments"
        description="Every appointment you've booked, newest first."
      />

      {appointments.length === 0 ? (
        <Card>
          <CardBody>
            <EmptyState
              icon={<i className="bx bxs-calendar-check" aria-hidden />}
              title="No appointments yet"
              description="Pick a doctor, choose a time slot and book in a couple of clicks."
              action={
                <Link
                  href="/doctors"
                  className="rounded-md bg-primary-900 px-4 py-2 text-sm font-medium text-white hover:bg-primary-800"
                >
                  Browse doctors
                </Link>
              }
            />
          </CardBody>
        </Card>
      ) : (
        <div className="flex flex-col gap-6">
          {active.length > 0 ? (
            <Card>
              <CardBody className="flex flex-col gap-3">
                {active.map((a) => (
                  <div
                    key={a.id}
                    className="flex items-center justify-between gap-4 rounded-md border border-border p-4"
                  >
                    <div className="min-w-0">
                      <p className="font-medium text-text-primary">
                        {a.doctorName}
                      </p>
                      <p className="text-sm text-highlight">
                        {a.specialization}
                      </p>
                      <p className="mt-1 flex items-center gap-2 text-sm text-text-muted">
                        <i className="bx bxs-calendar" aria-hidden />
                        {a.appointmentDate}
                        <span aria-hidden>·</span>
                        <i className="bx bx-time" aria-hidden />
                        {a.timeSlot}
                      </p>
                    </div>

                    <div className="flex shrink-0 flex-col items-end gap-2">
                      <StatusBadge status={a.status} />
                      {a.type === "TELEMEDICINE" ? (
                        <span className="text-xs text-text-muted">
                          Telemedicine
                        </span>
                      ) : (
                        <span className="text-xs text-text-muted">
                          In person
                        </span>
                      )}
                    </div>
                  </div>
                ))}
              </CardBody>
            </Card>
          ) : null}

          {past.length > 0 ? (
            <Card>
              <div className="border-b border-border px-5 py-4">
                <h2 className="text-base font-semibold text-text-primary">
                  Cancelled
                </h2>
              </div>
              <CardBody className="flex flex-col gap-3">
                {past.map((a) => (
                  <div
                    key={a.id}
                    className="flex items-center justify-between gap-4 rounded-md border border-border p-4 opacity-60"
                  >
                    <div className="min-w-0">
                      <p className="font-medium text-text-primary">
                        {a.doctorName}
                      </p>
                      <p className="text-sm text-text-muted">
                        {a.appointmentDate} · {a.timeSlot}
                      </p>
                    </div>
                    <StatusBadge status={a.status} />
                  </div>
                ))}
              </CardBody>
            </Card>
          ) : null}
        </div>
      )}
    </AppShell>
  );
}