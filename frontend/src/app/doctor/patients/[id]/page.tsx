import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { BloodGroupBadge, StatusBadge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole, type CurrentUser } from "@/lib/auth";
import { fetchDoctorPatient } from "@/lib/doctor-api";
import { PatientRecordForm } from "./PatientRecordForm";

export const metadata: Metadata = { title: "Patient Record" };

export const instant = false;

export default async function DoctorPatientRecordPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const user = await requireRole(["DOCTOR"], `/doctor/patients/${id}`);
  const patientId = Number(id);

  if (!Number.isFinite(patientId)) {
    return <PatientNotFound user={user} />;
  }

  const patient = await fetchDoctorPatient(patientId);
  if (!patient) {
    return <PatientNotFound user={user} />;
  }

  return (
    <AppShell user={user}>
      <PageHeader
        title={patient.name ?? `Patient #${patient.id}`}
        description={`Patient ID ${patient.id}${patient.email ? ` · ${patient.email}` : ""}`}
        action={
          <div className="flex flex-wrap items-center gap-2">
            <Link href={`/doctor/prescriptions?patientId=${patient.id}`}>
              <Button size="sm">
                <i className="bx bxs-capsule" aria-hidden />
                Write prescription
              </Button>
            </Link>
            <Link href="/doctor/patients">
              <Button variant="secondary" size="sm">
                ← All patients
              </Button>
            </Link>
          </div>
        }
      />

      <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_320px]">
        <div className="min-w-0">
          <PatientRecordForm patient={patient} />
        </div>

        <aside className="flex flex-col gap-5">
          <Card>
            <CardHeader title="Chart summary" />
            <CardBody className="flex flex-col gap-3 text-sm">
              <div className="flex items-center justify-between gap-3">
                <span className="text-text-muted">Blood group</span>
                <BloodGroupBadge group={patient.bloodGroup} />
              </div>
              <div className="flex items-center justify-between gap-3">
                <span className="text-text-muted">Age / gender</span>
                <span className="font-medium text-text-primary">
                  {patient.age ?? "—"} / {genderLabel(patient.gender)}
                </span>
              </div>
              <div className="flex items-center justify-between gap-3">
                <span className="text-text-muted">Location</span>
                <span className="text-right font-medium text-text-primary">
                  {[patient.zilla, patient.division].filter(Boolean).join(", ") || "—"}
                </span>
              </div>
              <div className="flex items-center justify-between gap-3">
                <span className="text-text-muted">Appointments</span>
                <span className="font-medium text-text-primary">
                  {patient.appointmentCount}
                </span>
              </div>
              <div className="flex items-center justify-between gap-3">
                <span className="text-text-muted">Last visit</span>
                <span className="font-medium text-text-primary">
                  {patient.lastVisit ?? "—"}
                </span>
              </div>
              <div className="flex items-center justify-between gap-3">
                <span className="text-text-muted">Relationship</span>
                <span
                  className={[
                    "rounded-full px-2 py-0.5 text-[11px] font-semibold",
                    patient.myPatient
                      ? "bg-success-soft text-success"
                      : "bg-neutral-100 text-text-muted",
                  ].join(" ")}
                >
                  {patient.myPatient ? "My patient" : "Directory lookup"}
                </span>
              </div>
              <p className="border-t border-border pt-3 text-xs text-text-muted">
                Last updated {patient.updatedAt ?? "—"}
              </p>
            </CardBody>
          </Card>

          <Card>
            <CardHeader title="Appointments with you" />
            <CardBody className="flex flex-col gap-3">
              {patient.appointments.length === 0 ? (
                <p className="text-sm text-text-muted">
                  No appointments booked with you yet.
                </p>
              ) : (
                patient.appointments.map((appointment) => (
                  <div
                    key={appointment.id}
                    className="rounded-lg border border-border p-3 text-sm"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="font-medium text-text-primary">
                        {appointment.appointmentDate ?? "—"}
                      </span>
                      <StatusBadge status={appointment.status} />
                    </div>
                    <p className="mt-1 text-xs text-text-muted">
                      {appointment.timeSlot ?? "—"} ·{" "}
                      {appointment.type?.toLowerCase() ?? "—"}
                    </p>
                    {appointment.status === "CONFIRMED" &&
                    appointment.telemedicineLink ? (
                      <a
                        href={appointment.telemedicineLink}
                        target="_blank"
                        rel="noreferrer"
                        className="mt-2 inline-flex items-center gap-1 text-xs font-medium text-primary-600 hover:underline"
                      >
                        <i className="bx bx-video" aria-hidden />
                        Join meeting
                      </a>
                    ) : null}
                  </div>
                ))
              )}
            </CardBody>
          </Card>
        </aside>
      </div>
    </AppShell>
  );
}

function genderLabel(gender: string | null): string {
  switch (gender) {
    case "F":
      return "Female";
    case "M":
      return "Male";
    case "O":
      return "Other";
    default:
      return "Unknown";
  }
}

function PatientNotFound({ user }: { user: CurrentUser }) {
  return (
    <AppShell user={user}>
      <div className="mx-auto w-full max-w-xl py-10">
        <Card>
          <CardBody>
            <EmptyState
              icon={<i className="bx bxs-user-x" aria-hidden />}
              title="Patient not found"
              description="Check the patient ID and try again, or open your patient list."
              action={
                <Link href="/doctor/patients">
                  <Button variant="secondary" size="sm">
                    Back to patients
                  </Button>
                </Link>
              }
            />
          </CardBody>
        </Card>
      </div>
    </AppShell>
  );
}
