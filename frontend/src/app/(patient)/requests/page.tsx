import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { BloodGroupBadge, StatusBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState, SuccessMessage } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchMyRequests } from "@/lib/patient-api";

export const metadata: Metadata = { title: "My Blood Requests" };

export const instant = false;

export default async function RequestsPage({
  searchParams,
}: {
  searchParams: Promise<{ created?: string; matched?: string }>;
}) {
  const user = await requireRole(["PATIENT"], "/requests");
  const requests = await fetchMyRequests();
  const { created, matched } = await searchParams;
  const matchedCount = Number(matched ?? 0);

  return (
    <AppShell user={user}>
      <PageHeader
        title="My Blood Requests"
        description="Requests you've posted, and whether a donor has been matched."
        action={
          <Link
            href="/requests/new"
            className="rounded-md bg-primary-900 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-800"
          >
            Post a request
          </Link>
        }
      />

      {created ? (
        <div className="mb-4">
          <SuccessMessage>
            Your request has been posted.{" "}
            {matchedCount > 0
              ? `${matchedCount} eligible donor${matchedCount === 1 ? "" : "s"} already registered for this group.`
              : "We'll notify you as soon as a matching donor is available."}
          </SuccessMessage>
        </div>
      ) : null}

      {requests.length === 0 ? (
        <Card>
          <CardBody>
            <EmptyState
              icon={<i className="bx bxs-droplet" aria-hidden />}
              title="You haven't posted a request yet"
              description="Post one and we'll match you with eligible donors in your area."
              action={
                <Link
                  href="/requests/new"
                  className="rounded-md bg-primary-900 px-4 py-2 text-sm font-medium text-white hover:bg-primary-800"
                >
                  Post a blood request
                </Link>
              }
            />
          </CardBody>
        </Card>
      ) : (
        <Card>
          <CardBody>
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-border text-xs uppercase tracking-wide text-text-muted">
                  <th className="pb-3 pr-4 font-medium">Blood group</th>
                  <th className="pb-3 pr-4 font-medium">Units</th>
                  <th className="pb-3 pr-4 font-medium">Location</th>
                  <th className="pb-3 pr-4 font-medium">Needed by</th>
                  <th className="pb-3 pr-4 font-medium">Reason</th>
                  <th className="pb-3 font-medium">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {requests.map((r) => (
                  <tr key={r.id}>
                    <td className="py-3 pr-4">
                      <BloodGroupBadge group={r.bloodGroup} />
                    </td>
                    <td className="py-3 pr-4 text-text-primary">{r.units}</td>
                    <td className="py-3 pr-4 text-text-secondary">
                      {[r.district, r.thana].filter(Boolean).join(", ") ||
                        "—"}
                    </td>
                    <td className="py-3 pr-4 text-text-secondary">
                      {r.requiredDate ?? "—"}
                    </td>
                    <td className="max-w-xs truncate py-3 pr-4 text-text-secondary">
                      {r.medicalReason ?? "—"}
                    </td>
                    <td className="py-3">
                      <StatusBadge status={r.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardBody>
        </Card>
      )}
    </AppShell>
  );
}