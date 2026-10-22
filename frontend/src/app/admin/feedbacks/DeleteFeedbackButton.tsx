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
          className="rounded-md border border-border-strong bg-surface px-3 py-1.5 text-xs font-medium text-text-primary hover:bg-neutral-50 disabled:opacity-60"
        >
          {pending ? "…" : "Delete"}
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