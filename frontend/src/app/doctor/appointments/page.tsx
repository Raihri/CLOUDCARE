import type { Metadata } from "next";

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
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-border text-xs uppercase tracking-wide text-text-muted">
                  <th className="pb-3 pr-4 font-medium">Patient</th>
                  <th className="pb-3 pr-4 font-medium">Date</th>
                  <th className="pb-3 pr-4 font-medium">Time</th>
                  <th className="pb-3 pr-4 font-medium">Type</th>
                  <th className="pb-3 pr-4 font-medium">Status</th>
                  <th className="pb-3 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {appointments.map((a) => (
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
                    <td className="py-3 pr-4">
                      <StatusBadge status={a.status} />
                    </td>
                    <td className="py-3">
                      <AppointmentActions
                        appointmentId={a.id}
                        status={a.status}
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardBody>
      </Card>
    </AppShell>
  );
}