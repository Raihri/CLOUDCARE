import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { StatusBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchDoctorAppointments } from "@/lib/doctor-api";
import { AppointmentActions } from "./AppointmentActions";

export const metadata: Metadata = { title: "Doctor Appointments" };

export const instant = false;

export default async function DoctorAppointmentsPage() {
  const user = await requireRole(["DOCTOR"], "/doctor/appointments");
  const appointments = await fetchDoctorAppointments();

  return (
    <AppShell user={user}>
      <PageHeader
        title="Appointments"
        description="Confirm or cancel bookings. Cancelling frees the slot for someone else."
      />

      <Card>
        <CardBody>
          {appointments.length === 0 ? (
            <EmptyState
              icon={<i className="bx bxs-calendar" aria-hidden />}
              title="No appointments yet"
              description="When a patient books one of your slots it will show up here."
            />
          ) : (
            <div className="divide-y divide-border">
              {appointments.map((a) => (
                <div
                  key={a.id}
                  className="flex flex-col gap-3 py-4 first:pt-0 last:pb-0"
                >
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div className="min-w-0">
                      <Link
                        href={`/doctor/patients/${a.patientId}`}
                        className="font-medium text-text-primary hover:text-primary-600 hover:underline"
                      >
                        {a.patientName ?? "—"}
                      </Link>
                      <p className="mt-1 text-xs text-text-muted">
                        {a.appointmentDate ?? "—"} · {a.timeSlot ?? "—"} ·{" "}
                        {a.type?.toLowerCase() ?? "—"}
                      </p>
                    </div>
                    <StatusBadge status={a.status} />
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    <AppointmentActions
                      appointmentId={a.id}
                      status={a.status}
                    />
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
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardBody>
      </Card>
    </AppShell>
  );
}