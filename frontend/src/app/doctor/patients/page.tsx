import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { BloodGroupBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchDoctorPatients } from "@/lib/doctor-api";
import { PatientSearch } from "./PatientSearch";

export const metadata: Metadata = { title: "My Patients" };

export const instant = false;

export default async function DoctorPatientsPage() {
  const user = await requireRole(["DOCTOR"], "/doctor/patients");
  const patients = await fetchDoctorPatients();

  return (
    <AppShell user={user}>
      <PageHeader
        title="Patients"
        description="Look up any patient by ID, or review everyone you've seen."
      />

      <Card>
        <CardBody>
          <PatientSearch />
        </CardBody>
      </Card>

      <Card>
        <CardBody>
          {patients.length === 0 ? (
            <EmptyState
              icon={<i className="bx bxs-user" aria-hidden />}
              title="No patients yet"
              description="Patients appear here once they book one of your slots."
            />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full min-w-[680px] text-left text-sm">
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
                    <tr key={p.id} className="hover:bg-neutral-50">
                      <td className="py-3 pr-4">
                        <Link
                          href={`/doctor/patients/${p.id}`}
                          className="font-medium text-text-primary hover:text-primary-600 hover:underline"
                        >
                          {p.name ?? "—"}
                        </Link>
                        <p className="text-xs text-text-muted">
                          ID {p.id}
                          {p.email ? ` · ${p.email}` : ""}
                        </p>
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
            </div>
          )}
        </CardBody>
      </Card>
    </AppShell>
  );
}