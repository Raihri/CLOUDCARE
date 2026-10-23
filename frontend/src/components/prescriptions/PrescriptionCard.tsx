import Link from "next/link";

import { Badge } from "@/components/ui/Badge";

type PrescriptionLike = {
  id: number;
  patientId: number | null;
  patientName: string | null;
  doctorName: string | null;
  doctorSpecialization: string | null;
  hospitalName: string | null;
  diagnosis: string | null;
  medicines: string;
  instructions: string | null;
  followUpDate: string | null;
  createdAt: string | null;
  daysUntilFollowUp: number | null;
  followUpStatus: "NONE" | "OVERDUE" | "DUE_TODAY" | "UPCOMING" | "SCHEDULED";
};

const STATUS_TONE = {
  NONE: "neutral",
  OVERDUE: "danger",
  DUE_TODAY: "danger",
  UPCOMING: "warning",
  SCHEDULED: "info",
} as const;

function followUpLabel(p: PrescriptionLike): string {
  if (!p.followUpDate) return "No follow-up set";
  if (p.followUpStatus === "DUE_TODAY") return `Follow-up today · ${p.followUpDate}`;
  if (p.followUpStatus === "OVERDUE") {
    const days = Math.abs(p.daysUntilFollowUp ?? 0);
    return `Follow-up overdue by ${days} day${days === 1 ? "" : "s"} · ${p.followUpDate}`;
  }
  if (p.followUpStatus === "UPCOMING") {
    const days = p.daysUntilFollowUp ?? 0;
    return `Follow-up in ${days} day${days === 1 ? "" : "s"} · ${p.followUpDate}`;
  }
  return `Follow-up ${p.followUpDate}`;
}

function displayName(name: string | null, fallback: string): string {
  if (!name) return fallback;
  const trimmed = name.trim();
  if (!trimmed) return fallback;
  return trimmed.startsWith("Dr.") ? trimmed : `Dr. ${trimmed}`;
}

/**
 * One prescription, shared by the doctor history and the patient page.
 *
 * `showPatient` flips the identity line: the doctor needs to see who the
 * prescription is for, while the patient needs to see who wrote it.
 */
export function PrescriptionCard({
  prescription,
  showPatient = false,
}: {
  prescription: PrescriptionLike;
  showPatient?: boolean;
}) {
  return (
    <article className="rounded-lg border border-border p-4">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div className="min-w-0">
          {showPatient ? (
            <p className="font-medium text-text-primary">
              {prescription.patientName ?? `Patient #${prescription.patientId ?? "—"}`}
              {prescription.patientId ? (
                <Link
                  href={`/doctor/patients/${prescription.patientId}`}
                  className="ml-2 text-xs font-normal text-highlight hover:underline"
                >
                  Open chart
                </Link>
              ) : null}
            </p>
          ) : (
            <p className="font-medium text-text-primary">
              {displayName(prescription.doctorName, "Your doctor")}
            </p>
          )}
          <p className="mt-0.5 text-xs text-text-muted">
            {showPatient
              ? prescription.createdAt
                ? `Written ${prescription.createdAt.slice(0, 10)}`
                : "Prescription"
              : prescription.doctorSpecialization ?? "Prescription"}
            {!showPatient && prescription.hospitalName
              ? ` · ${prescription.hospitalName}`
              : ""}
          </p>
        </div>

        <Badge tone={STATUS_TONE[prescription.followUpStatus]}>
          {followUpLabel(prescription)}
        </Badge>
      </div>

      {prescription.diagnosis ? (
        <div className="mt-3">
          <p className="text-[11px] font-semibold tracking-wide text-text-muted uppercase">
            Diagnosis
          </p>
          <p className="mt-0.5 text-sm text-text-secondary">
            {prescription.diagnosis}
          </p>
        </div>
      ) : null}

      <div className="mt-3">
        <p className="text-[11px] font-semibold tracking-wide text-text-muted uppercase">
          Medicines & treatment
        </p>
        <p className="mt-0.5 whitespace-pre-line text-sm text-text-primary">
          {prescription.medicines}
        </p>
      </div>

      {prescription.instructions ? (
        <div className="mt-3 rounded-md bg-neutral-50 px-3 py-2">
          <p className="text-[11px] font-semibold tracking-wide text-text-muted uppercase">
            Instructions
          </p>
          <p className="mt-0.5 whitespace-pre-line text-sm text-text-secondary">
            {prescription.instructions}
          </p>
        </div>
      ) : null}
    </article>
  );
}
