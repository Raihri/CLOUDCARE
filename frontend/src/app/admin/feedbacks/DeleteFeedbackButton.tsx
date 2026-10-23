"use client";

import { useActionState } from "react";

import { deleteFeedback, type AdminActionState } from "../actions";

export function DeleteFeedbackButton({ feedbackId }: { feedbackId: number }) {
  const [state, formAction, pending] = useActionState<AdminActionState, FormData>(
    deleteFeedback,
    undefined,
  );

  return (
    <div className="flex shrink-0 flex-col items-end gap-1">
      <form action={formAction}>
        <input type="hidden" name="feedbackId" value={feedbackId} />
        <button
          type="submit"
          disabled={pending}
          aria-label="Delete feedback"
          className="inline-flex h-8 items-center gap-1.5 rounded-lg border border-danger/20 bg-danger-soft px-2.5 text-xs font-medium text-danger transition-colors hover:bg-danger/10 disabled:opacity-60"
        >
          <i aria-hidden className="bx bx-trash text-sm leading-none" />
          {pending ? "Deleting…" : "Delete"}
        </button>
      </form>
      {state ? (
        state.ok ? (
          <span className="text-xs text-success">{state.message}</span>
        ) : (
          <span className="text-xs text-danger">{state.message}</span>
        )
      ) : null}
    </div>
  );
}