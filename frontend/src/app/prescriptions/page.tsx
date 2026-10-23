import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { PrescriptionCard } from "@/components/prescriptions/PrescriptionCard";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchPrescriptions } from "@/lib/patient-api";

export const metadata: Metadata = { title: "Prescriptions" };

export const instant = false;

export default async function PrescriptionsPage() {
  const user = await requireRole(["PATIENT"], "/prescriptions");
  const prescriptions = await fetchPrescriptions();

  const upcoming = prescriptions
    .filter(
      (p) =>
        p.followUpDate &&
        p.followUpStatus !== "OVERDUE" &&
        p.followUpStatus !== "NONE",
    )
    .sort((a, b) => (a.followUpDate ?? "").localeCompare(b.followUpDate ?? ""));

  return (
    <AppShell user={user}>
      <PageHeader
        title="Prescriptions"
        description="Medicines, instructions and follow-up dates from your doctors."
        action={
          <Link href="/doctors">
            <span className="rounded-md border border-primary-600 bg-surface px-3 py-1.5 text-sm font-medium text-primary-900 hover:bg-primary-50">
              Book a doctor
            </span>
          </Link>
        }
      />

      {prescriptions.length === 0 ? (
        <Card>
          <CardBody>
            <EmptyState
              icon={<i className="bx bxs-capsule" aria-hidden />}
              title="No prescriptions yet"
              description="When a doctor writes a prescription for you, it will appear here with the next follow-up date."
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
        <div className="flex flex-col gap-5">
          {upcoming.length > 0 ? (
            <Card className="border-primary-200 bg-primary-50/60">
              <CardBody className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="text-sm font-semibold text-text-primary">
                    Next follow-up
                  </p>
                  <p className="mt-0.5 text-sm text-text-secondary">
                    {upcoming[0].doctorName ?? "Your doctor"} ·{" "}
                    {upcoming[0].followUpDate}
                  </p>
                </div>
                <span className="text-xs font-medium text-highlight">
                  {upcoming[0].daysUntilFollowUp === 0
                    ? "Today"
                    : `In ${upcoming[0].daysUntilFollowUp} day${
                        upcoming[0].daysUntilFollowUp === 1 ? "" : "s"
                      }`}
                </span>
              </CardBody>
            </Card>
          ) : null}

          <Card>
            <CardHeader
              title="Your prescriptions"
              description={`${prescriptions.length} prescription${
                prescriptions.length === 1 ? "" : "s"
              }, newest first.`}
            />
            <CardBody>
              <div className="flex flex-col gap-3">
                {prescriptions.map((prescription) => (
                  <PrescriptionCard
                    key={prescription.id}
                    prescription={prescription}
                  />
                ))}
              </div>
            </CardBody>
          </Card>
        </div>
      )}
    </AppShell>
  );
}
