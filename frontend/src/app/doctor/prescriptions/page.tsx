import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { PrescriptionCard } from "@/components/prescriptions/PrescriptionCard";
import { BloodGroupBadge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchDoctorPatient, fetchDoctorPrescriptions } from "@/lib/doctor-api";
import { PrescriptionForm } from "./PrescriptionForm";
import { PrescriptionPatientSearch } from "./PrescriptionPatientSearch";

export const metadata: Metadata = { title: "Prescriptions" };

export const instant = false;

export default async function DoctorPrescriptionsPage({
  searchParams,
}: {
  searchParams: Promise<{ patientId?: string | string[] }>;
}) {
  const user = await requireRole(["DOCTOR"], "/doctor/prescriptions");
  const params = await searchParams;
  const rawPatientId = Array.isArray(params.patientId)
    ? params.patientId[0]
    : params.patientId;

  const patientId =
    rawPatientId && /^\d+$/.test(rawPatientId) ? Number(rawPatientId) : null;

  const [patient, prescriptions] = await Promise.all([
    patientId !== null ? fetchDoctorPatient(patientId) : Promise.resolve(null),
    fetchDoctorPrescriptions(patientId ?? undefined),
  ]);

  return (
    <AppShell user={user}>
      <PageHeader
        title="Prescriptions"
        description="Look up a patient, review their chart and write the next prescription with a follow-up date."
        action={
          <Link href="/doctor/patients">
            <Button variant="secondary" size="sm">
              ← All patients
            </Button>
          </Link>
        }
      />

      <Card>
        <CardBody>
          <PrescriptionPatientSearch defaultPatientId={rawPatientId ?? ""} />
        </CardBody>
      </Card>

      {patientId !== null && !patient ? (
        <Card>
          <CardBody>
            <EmptyState
              icon={<i className="bx bxs-user-x" aria-hidden />}
              title={`No patient found with ID ${patientId}`}
              description="Check the numeric ID and try again, or open your patient list."
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
      ) : null}

      {patient ? (
        <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_340px]">
          <div className="flex min-w-0 flex-col gap-5">
            <PrescriptionForm patientId={patient.id} patientName={patient.name} />

            <Card>
              <CardHeader
                title="Prescription history"
                description={`${prescriptions.length} prescription${
                  prescriptions.length === 1 ? "" : "s"
                } for this patient.`}
              />
              <CardBody>
                {prescriptions.length === 0 ? (
                  <EmptyState
                    icon={<i className="bx bxs-capsule" aria-hidden />}
                    title="No prescriptions yet"
                    description="The prescription you save above will appear here."
                  />
                ) : (
                  <div className="flex flex-col gap-3">
                    {prescriptions.map((prescription) => (
                      <PrescriptionCard
                        key={prescription.id}
                        prescription={prescription}
                        showPatient={false}
                      />
                    ))}
                  </div>
                )}
              </CardBody>
            </Card>
          </div>

          <aside className="flex flex-col gap-5">
            <Card>
              <CardHeader
                title="Patient summary"
                action={
                  <Link
                    href={`/doctor/patients/${patient.id}`}
                    className="text-sm font-medium text-highlight hover:underline"
                  >
                    Full chart →
                  </Link>
                }
              />
              <CardBody className="flex flex-col gap-3 text-sm">
                <div>
                  <p className="font-medium text-text-primary">
                    {patient.name ?? `Patient #${patient.id}`}
                  </p>
                  <p className="text-xs text-text-muted">
                    ID {patient.id}
                    {patient.email ? ` · ${patient.email}` : ""}
                  </p>
                </div>

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
                    {[patient.zilla, patient.division].filter(Boolean).join(", ") ||
                      "—"}
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

                {patient.allergies.length > 0 ? (
                  <div className="border-t border-border pt-3">
                    <p className="text-xs font-semibold tracking-wide text-text-muted uppercase">
                      Allergies
                    </p>
                    <p className="mt-1 text-text-secondary">
                      {patient.allergies.join(", ")}
                    </p>
                  </div>
                ) : null}

                {patient.diseases.length > 0 ? (
                  <div>
                    <p className="text-xs font-semibold tracking-wide text-text-muted uppercase">
                      Known conditions
                    </p>
                    <p className="mt-1 text-text-secondary">
                      {patient.diseases.join(", ")}
                    </p>
                  </div>
                ) : null}

                {patient.medications.length > 0 ? (
                  <div>
                    <p className="text-xs font-semibold tracking-wide text-text-muted uppercase">
                      Current medications
                    </p>
                    <p className="mt-1 text-text-secondary">
                      {patient.medications.join(", ")}
                    </p>
                  </div>
                ) : null}
              </CardBody>
            </Card>

            <Card>
              <CardHeader title="Recent appointments" />
              <CardBody className="flex flex-col gap-2 text-sm">
                {patient.appointments.length === 0 ? (
                  <p className="text-text-muted">
                    No appointments booked with you yet.
                  </p>
                ) : (
                  patient.appointments.slice(0, 4).map((appointment) => (
                    <div
                      key={appointment.id}
                      className="flex items-center justify-between gap-2 border-b border-border pb-2 last:border-0 last:pb-0"
                    >
                      <span className="text-text-secondary">
                        {appointment.appointmentDate ?? "—"} ·{" "}
                        {appointment.timeSlot ?? "—"}
                      </span>
                      <span className="text-xs font-medium text-text-muted">
                        {appointment.status ?? "—"}
                      </span>
                    </div>
                  ))
                )}
              </CardBody>
            </Card>
          </aside>
        </div>
      ) : null}

      {patientId === null ? (
        <Card>
          <CardHeader
            title="Recent prescriptions"
            description="The latest prescriptions you've written. Search a patient ID above to write a new one."
          />
          <CardBody>
            {prescriptions.length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-capsule" aria-hidden />}
                title="No prescriptions yet"
                description="Search for a patient by ID to write your first prescription."
              />
            ) : (
              <div className="flex flex-col gap-3">
                {prescriptions.slice(0, 10).map((prescription) => (
                  <PrescriptionCard
                    key={prescription.id}
                    prescription={prescription}
                    showPatient
                  />
                ))}
              </div>
            )}
          </CardBody>
        </Card>
      ) : null}
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
