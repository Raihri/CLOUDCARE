import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody } from "@/components/ui/Card";
import { ErrorMessage } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { springJson } from "@/lib/api";
import { HealthForms } from "./HealthForms";
import type { HealthRecord } from "@/lib/health-record";

export const metadata: Metadata = { title: "Health Records" };

export const instant = false;

export default async function HealthRecordsPage() {
  const user = await requireRole(["PATIENT"], "/health-records");

  // A read failure here means the whole page has nothing to show, so unlike the
  // dashboard panels this one surfaces the error instead of degrading to empty.
  let record: HealthRecord | null = null;
  let error: string | null = null;
  try {
    record = await springJson<HealthRecord>("/patient/api/health");
  } catch {
    error = "We couldn't load your health record. Please refresh and try again.";
  }

  return (
    <AppShell user={user}>
      <PageHeader
        title="Health Records"
        description="Keep your details current so your doctors have the full picture."
      />

      {error || !record ? (
        <Card>
          <CardBody>
            <ErrorMessage>{error ?? "No record found."}</ErrorMessage>
          </CardBody>
        </Card>
      ) : (
        <HealthForms record={record} />
      )}
    </AppShell>
  );
}