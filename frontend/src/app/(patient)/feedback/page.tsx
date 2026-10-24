import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody } from "@/components/ui/Card";
import { SuccessMessage } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { FeedbackForm } from "./FeedbackForm";

export const metadata: Metadata = { title: "Patient Feedback" };

export const instant = false;

export default async function FeedbackPage({
  searchParams,
}: {
  searchParams: Promise<{ sent?: string }>;
}) {
  const user = await requireRole(["PATIENT"], "/feedback");
  const { sent } = await searchParams;

  return (
    <AppShell user={user}>
      <PageHeader
        title="Patient Feedback"
        description="Your feedback helps us make CloudCare better."
      />

      <div className="max-w-2xl">
        {sent ? (
          <div className="mb-4">
            <SuccessMessage>
              Thanks — your feedback has been recorded.
            </SuccessMessage>
          </div>
        ) : null}

        <Card>
          <CardBody>
            <FeedbackForm />
          </CardBody>
        </Card>
      </div>
    </AppShell>
  );
}