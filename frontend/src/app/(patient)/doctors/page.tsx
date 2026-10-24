import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { BloodGroupBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchDoctors } from "@/lib/patient-api";

export const metadata: Metadata = { title: "Doctors" };

export const instant = false;

export default async function DoctorsPage() {
  const user = await requireRole(["PATIENT"], "/doctors");
  const doctors = await fetchDoctors();

  return (
    <AppShell user={user}>
      <PageHeader
        title="Doctors List"
        description="Browse verified doctors and book an appointment."
      />

      {doctors.length === 0 ? (
        <Card>
          <CardBody>
            <EmptyState
              icon={<i className="bx bxs-user" aria-hidden />}
              title="No approved doctors yet"
              description="Doctors appear here once an administrator approves their application."
            />
          </CardBody>
        </Card>
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {doctors.map((doctor) => (
            <Link
              key={doctor.id}
              href={`/doctors/${doctor.id}`}
              className="group rounded-lg border border-border bg-surface p-5 shadow-sm transition-colors hover:border-primary-600"
            >
              <div className="flex items-start gap-3">
                <span className="flex size-12 shrink-0 items-center justify-center rounded-full bg-primary-50 text-lg font-semibold text-primary-700">
                  {doctor.name?.charAt(0).toUpperCase() ?? "D"}
                </span>

                <div className="min-w-0 flex-1">
                  <h2 className="truncate font-semibold text-text-primary group-hover:text-primary-700">
                    {doctor.name}
                  </h2>
                  <p className="truncate text-sm text-highlight">
                    {doctor.specialization || "General Medicine"}
                  </p>
                  {doctor.degrees ? (
                    <p className="mt-0.5 truncate text-xs text-text-muted">
                      {doctor.degrees}
                    </p>
                  ) : null}
                </div>

                {doctor.bloodGroup ? (
                  <BloodGroupBadge group={doctor.bloodGroup} />
                ) : null}
              </div>

              <dl className="mt-4 grid grid-cols-2 gap-3 border-t border-border pt-3 text-sm">
                <div>
                  <dt className="text-xs text-text-muted">Experience</dt>
                  <dd className="text-text-primary">
                    {doctor.experienceYears != null
                      ? `${doctor.experienceYears} yrs`
                      : "—"}
                  </dd>
                </div>
                <div>
                  <dt className="text-xs text-text-muted">Fee</dt>
                  <dd className="text-text-primary">
                    {doctor.consultationFee
                      ? `৳${doctor.consultationFee}`
                      : "—"}
                  </dd>
                </div>
                <div className="col-span-2">
                  <dt className="text-xs text-text-muted">Hospital</dt>
                  <dd className="truncate text-text-primary">
                    {doctor.hospitalName || doctor.zilla || "—"}
                  </dd>
                </div>
              </dl>

              {doctor.rating ? (
                <p className="mt-3 flex items-center gap-1 text-sm text-text-secondary">
                  <i className="bx bxs-star text-warning" aria-hidden />
                  {doctor.rating}
                </p>
              ) : null}
            </Link>
          ))}
        </div>
      )}
    </AppShell>
  );
}