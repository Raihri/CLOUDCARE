import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody } from "@/components/ui/Card";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";

export const metadata: Metadata = { title: "Quiz Result" };

export const instant = false;

/** Matches QuizController.getStatus bands, so the colour agrees with the score
 *  the backend actually assigned rather than re-deriving it here. */
function toneFor(score: number): { label: string; className: string } {
  if (score <= 10) return { label: "Excellent", className: "text-success" };
  if (score <= 20) return { label: "Good", className: "text-azure" };
  if (score <= 30) return { label: "Moderate", className: "text-warning" };
  if (score <= 40) return { label: "Concerning", className: "text-coral" };
  return { label: "Critical", className: "text-danger" };
}

export default async function QuizResultPage({
  searchParams,
}: {
  searchParams: Promise<{
    name?: string;
    score?: string;
    status?: string;
    suggestion?: string;
  }>;
}) {
  const user = await requireRole(["PATIENT"], "/quiz/result");
  const params = await searchParams;

  const name = params.name ?? user.name ?? "Anonymous";
  const score = Number(params.score ?? 0);
  const status = params.status ?? "No result found";
  const suggestion = params.suggestion ?? "";
  const tone = toneFor(score);

  // Query string is re-encoded so the download link stays safe regardless of
  // what came back from the server.
  const downloadQuery = new URLSearchParams({
    name,
    score: String(score),
    status,
    suggestion,
  }).toString();

  return (
    <AppShell user={user}>
      <PageHeader
        title="Your Result"
        description="A snapshot of how you've been feeling. It isn't a diagnosis."
        action={
          <Link
            href="/quiz"
            className="rounded-md bg-primary-900 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-800"
          >
            Retake the quiz
          </Link>
        }
      />

      <div className="max-w-3xl">
        <Card>
          <CardBody>
            <div className="flex items-baseline justify-between border-b border-border pb-4">
              <div>
                <p className="text-sm text-text-muted">Result for</p>
                <h2 className="text-lg font-semibold text-text-primary">{name}</h2>
              </div>
              <div className="text-right">
                <p className="text-sm text-text-muted">Total score</p>
                <p className={`text-2xl font-semibold ${tone.className}`}>{score}</p>
              </div>
            </div>

            <div className="py-5">
              <span
                className={`inline-block rounded-full bg-neutral-100 px-3 py-1 text-sm font-medium ${tone.className}`}
              >
                {status}
              </span>
            </div>

            {suggestion ? (
              <div className="rounded-md border border-border bg-neutral-50 p-4 text-sm text-text-secondary">
                {suggestion}
              </div>
            ) : null}

            <div className="mt-6 flex flex-wrap items-center gap-3 border-t border-border pt-5">
              <a
                href={`/api/spring/patient/download-pdf?${downloadQuery}`}
                className="rounded-md border border-border-strong bg-surface px-4 py-2 text-sm font-medium text-text-primary hover:bg-neutral-50"
              >
                <i className="bx bxs-file-pdf" aria-hidden /> Download PDF report
              </a>
              <p className="text-xs text-text-muted">
                If you're struggling, please talk to someone. You are not alone.
              </p>
            </div>
          </CardBody>
        </Card>
      </div>
    </AppShell>
  );
}