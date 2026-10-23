import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { Badge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchAdminFeedbacks } from "@/lib/admin-api";
import { FeedbackFilters } from "./FeedbackFilters";
import { DeleteFeedbackButton } from "./DeleteFeedbackButton";

export const metadata: Metadata = { title: "Patient Feedback" };

export const instant = false;

function initials(name: string | null) {
  if (!name) return "?";
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}

function formatDate(value: string | null) {
  if (!value) return "";
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) return value;
  return parsed.toLocaleDateString("en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

function StarRating({ value }: { value: number | null }) {
  const rating = Math.max(0, Math.min(5, Math.round(value ?? 0)));
  return (
    <div
      className="flex items-center gap-0.5 text-warning"
      aria-label={`${rating} out of 5`}
    >
      {[1, 2, 3, 4, 5].map((star) => (
        <i
          key={star}
          aria-hidden
          className={`bx ${star <= rating ? "bxs-star" : "bx-star"} text-sm leading-none`}
        />
      ))}
      <span className="ml-1 text-xs font-normal text-text-muted">
        {rating}/5
      </span>
    </div>
  );
}

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
              {feedbacks.map((feedback) => (
                <li key={feedback.id} className="flex gap-4 py-5">
                  <span className="flex size-10 shrink-0 items-center justify-center rounded-full bg-primary-50 text-xs font-semibold text-primary-700">
                    {initials(feedback.name)}
                  </span>

                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
                      <span className="text-sm font-medium text-text-primary">
                        {feedback.name ?? "Anonymous"}
                      </span>
                      {feedback.category ? (
                        <Badge tone="info">{feedback.category}</Badge>
                      ) : null}
                      <span className="ml-auto text-xs text-text-muted">
                        {formatDate(feedback.createdAt)}
                      </span>
                    </div>

                    {feedback.email ? (
                      <p className="text-xs text-text-muted">{feedback.email}</p>
                    ) : null}

                    <div className="mt-2">
                      <StarRating value={feedback.rating} />
                    </div>

                    <p className="mt-2 text-sm whitespace-pre-wrap text-text-secondary">
                      {feedback.comments ?? "—"}
                    </p>
                  </div>

                  <DeleteFeedbackButton feedbackId={feedback.id} />
                </li>
              ))}
            </ul>
          )}
        </CardBody>
      </Card>
    </AppShell>
  );
}
