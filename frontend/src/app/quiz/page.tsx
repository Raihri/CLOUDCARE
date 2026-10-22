import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { springJson } from "@/lib/api";
import { QuizForm } from "./QuizForm";

export const metadata: Metadata = { title: "Mental Health Quiz" };

export const instant = false;

type QuizPayload = { name: string; questions: string[]; options: string[] };

export default async function QuizPage({
  searchParams,
}: {
  searchParams: Promise<{ name?: string }>;
}) {
  const user = await requireRole(["PATIENT"], "/quiz");
  const { name } = await searchParams;
  const displayName = name?.trim() || user.name || "Anonymous";

  // On failure fall back to an empty question list rather than erroring the
  // page — the empty state explains it better than a stack trace would.
  let quiz: QuizPayload = { name: displayName, questions: [], options: [] };
  try {
    quiz = await springJson<QuizPayload>(
      `/patient/api/quiz?name=${encodeURIComponent(displayName)}`,
    );
  } catch {
    // keep the empty fallback
  }

  return (
    <AppShell user={user}>
      <PageHeader
        title="Mental Health Check-in"
        description={`A short self-assessment for ${quiz.name}. Answer honestly — the result stays private to you.`}
      />

      {quiz.questions.length === 0 ? (
        <Card>
          <CardBody>
            <EmptyState
              icon={<i className="bx bxs-brain" aria-hidden />}
              title="No questions available"
              description="The assessment question bank couldn't be loaded. Please try again shortly."
            />
          </CardBody>
        </Card>
      ) : (
        <QuizForm initial={quiz} />
      )}
    </AppShell>
  );
}