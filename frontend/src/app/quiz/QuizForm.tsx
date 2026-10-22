"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

import { Button } from "@/components/ui/Button";
import { springFetch } from "@/lib/api-client";

type Quiz = {
  name: string;
  questions: string[];
  options: string[];
};

/**
 * Quiz UI ported from templates/quiz.html.
 *
 * <p>Scoring happens on the server (POST /patient/api/quiz/result) rather than
 * in the browser, so the score bands stay in one place — QuizController — and
 * can't drift between this page and the PDF report.
 */
export function QuizForm({ initial }: { initial: Quiz }) {
  const router = useRouter();
  const [answers, setAnswers] = useState<Record<number, string>>({});
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  const answered = Object.keys(answers).length;
  const total = initial.questions.length;
  const complete = total > 0 && answered === total;

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (!complete || pending) return;

    setPending(true);
    setError(null);

    const body = new URLSearchParams({ name: initial.name });
    for (const [i, choice] of Object.entries(answers)) {
      body.set(`q${i + 1}`, choice);
    }

    try {
      const res = await springFetch("/patient/api/quiz/result", {
        method: "POST",
        body,
      });
      const data = await res.json();
      if (!res.ok || data?.success === false) {
        setError(data?.message ?? "Could not score the quiz.");
        setPending(false);
        return;
      }
      // The PDF route reads the result back out of the Spring session, so the
      // download link works as long as that session survives the navigation.
      router.push(
        `/quiz/result?name=${encodeURIComponent(initial.name)}&score=${data.score}&status=${encodeURIComponent(data.status)}&suggestion=${encodeURIComponent(data.suggestion)}`,
      );
    } catch {
      setError("Could not reach the server. Please try again.");
      setPending(false);
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-5">
      <div className="flex items-center justify-between rounded-md border border-border bg-neutral-50 px-4 py-3 text-sm">
        <span className="text-text-secondary">
          Answered {answered} of {total}
        </span>
        <div className="h-2 w-40 overflow-hidden rounded-full bg-neutral-200">
          <div
            className="h-full rounded-full bg-azure transition-all"
            style={{ width: `${total ? (answered / total) * 100 : 0}%` }}
          />
        </div>
      </div>

      {initial.questions.map((question, i) => (
        <fieldset key={i} className="rounded-lg border border-border bg-surface p-5 shadow-sm">
          <legend className="px-1 text-sm font-medium text-text-primary">
            {i + 1}. {question}
          </legend>
          <div className="mt-3 flex flex-col gap-2">
            {initial.options.map((option) => (
              <label
                key={option}
                className="flex cursor-pointer items-center gap-3 rounded-md border border-border px-3 py-2 text-sm text-text-secondary transition-colors hover:border-azure hover:bg-neutral-50"
              >
                <input
                  type="radio"
                  name={`q${i + 1}`}
                  value={option}
                  checked={answers[i] === option}
                  onChange={() =>
                    setAnswers((prev) => ({ ...prev, [i]: option }))
                  }
                  className="accent-azure"
                />
                {option}
              </label>
            ))}
          </div>
        </fieldset>
      ))}

      {error ? <p className="text-sm text-danger">{error}</p> : null}

      <div className="flex items-center gap-3">
        <Button type="submit" disabled={!complete || pending}>
          {pending ? "Scoring…" : "See my result"}
        </Button>
        {!complete ? (
          <span className="text-sm text-text-muted">
            Answer every question to continue.
          </span>
        ) : null}
      </div>
    </form>
  );
}