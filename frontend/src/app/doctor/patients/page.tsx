import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { BloodGroupBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchDoctorPatients } from "@/lib/doctor-api";

export const metadata: Metadata = { title: "My Patients" };

export const instant = false;

export default async function DoctorPatientsPage() {
  const user = await requireRole(["DOCTOR"], "/doctor/patients");
  const patients = await fetchDoctorPatients();

  return (
    <AppShell user={user}>
      <PageHeader
        title="My Patients"
        description="Everyone you've booked an appointment with."
      />

      <Card>
        <CardBody>
          {patients.length === 0 ? (
            <EmptyState
              icon={<i className="bx bxs-user" aria-hidden />}
              title="No patients yet"
              description="Patients appear here once they book one of your slots."
            />
          ) : (
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-border text-xs uppercase tracking-wide text-text-muted">
                  <th className="pb-3 pr-4 font-medium">Name</th>
                  <th className="pb-3 pr-4 font-medium">Blood group</th>
                  <th className="pb-3 pr-4 font-medium">Age</th>
                  <th className="pb-3 pr-4 font-medium">District</th>
                  <th className="pb-3 pr-4 font-medium">Appointments</th>
                  <th className="pb-3 font-medium">Last visit</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {patients.map((p) => (
                  <tr key={p.id}>
                    <td className="py-3 pr-4">
                      <p className="font-medium text-text-primary">
                        {p.name ?? "—"}
                      </p>
                      <p className="text-xs text-text-muted">{p.email ?? ""}</p>
                    </td>
                    <td className="py-3 pr-4">
                      <BloodGroupBadge group={p.bloodGroup} />
                    </td>
                    <td className="py-3 pr-4 text-text-secondary">
                      {p.age ?? "—"}
                    </td>
                    <td className="py-3 pr-4 text-text-secondary">
                      {p.zilla ?? "—"}
                    </td>
                    <td className="py-3 pr-4 text-text-secondary">
                      {p.appointments}
                    </td>
                    <td className="py-3 text-text-secondary">
                      {p.lastVisit ?? "—"}
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