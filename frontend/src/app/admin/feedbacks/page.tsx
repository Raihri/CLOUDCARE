import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchAdminFeedbacks } from "@/lib/admin-api";
import { FeedbackFilters } from "./FeedbackFilters";
import { DeleteFeedbackButton } from "./DeleteFeedbackButton";

export const metadata: Metadata = { title: "Patient Feedback" };

export const instant = false;

export default async function AdminFeedbacksPage({
  searchParams,
}: {
  searchParams: Promise<{ name?: string; email?: string; category?: string }>;
}) {
  const user = await requireRole(["ADMIN"], "/admin/feedbacks");
  const params = await searchParams;

  const feedbacks = await fetchAdminFeedbacks({
    name: params.name ?? "",
    email: params.email ?? "",
    category: params.category ?? "",
  });

  return (
    <AppShell user={user}>
      <PageHeader
        title="Patient Feedback"
        description="Everything patients have submitted, newest first."
      />

      <FeedbackFilters current={params} />

      <div className="mt-4">
        <Card>
          <CardBody>
            {feedbacks.length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-star" aria-hidden />}
                title="No feedback found"
                description="Try widening the filters, or check back after patients submit some."
              />
            ) : (
              <ul className="divide-y divide-border">
                {feedbacks.map((f) => (
                  <li key={f.id} className="py-4">
                    <div className="flex items-start justify-between gap-4">
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-medium text-text-primary">
                            {f.name ?? "Anonymous"}
                          </span>
                          {f.category ? (
                            <span className="rounded-full bg-azure/10 px-2 py-0.5 text-xs text-azure">
                              {f.category}
                            </span>
                          ) : null}
                          <span className="text-xs text-text-muted">
                            {f.rating ?? 0}/5 · {f.createdAt ?? ""}
                          </span>
                        </div>
                        {f.email ? (
                          <p className="text-xs text-text-muted">{f.email}</p>
                        ) : null}
                        <p className="mt-2 text-sm whitespace-pre-wrap text-text-secondary">
                          {f.comments ?? "—"}
                        </p>
                      </div>
                      <DeleteFeedbackButton feedbackId={f.id} />
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </CardBody>
        </Card>
      </div>
    </AppShell>
  );
}