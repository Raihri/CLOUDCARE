"use client";

import { useActionState } from "react";

import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { setDoctorStatus, type AdminActionState } from "../actions";

/**
 * Approve / reject controls for one doctor row.
 *
 * Rejection asks for a reason inline, because the reason is what the applicant
 * sees — the backend stores it rather than a fixed string.
 */
export function DoctorRowActions({
  doctorId,
  status,
}: {
  doctorId: number;
  status: string | null;
}) {
  const [state, formAction, pending] = useActionState<AdminActionState, FormData>(
    setDoctorStatus,
    undefined,
  );

  if (status === "APPROVED") {
    return (
      <span className="inline-flex items-center gap-1 text-xs font-medium text-success">
        <i aria-hidden className="bx bxs-check-circle text-sm leading-none" />
        Active
      </span>
    );
  }

  if (status === "REJECTED") {
    return (
      <div className="flex flex-col items-start gap-2">
        <form action={formAction} className="flex gap-2">
          <input type="hidden" name="doctorId" value={doctorId} />
          <button
            type="submit"
            name="status"
            value="APPROVED"
            disabled={pending}
            className="rounded-md bg-primary-900 px-3 py-1.5 text-xs font-medium text-white hover:bg-primary-800 disabled:opacity-60"
          >
            Approve
          </button>
        </form>
        {state ? (
          state.ok ? (
            <SuccessMessage>{state.message}</SuccessMessage>
          ) : (
            <ErrorMessage>{state.message}</ErrorMessage>
          )
        ) : null}
      </div>
    );
  }

  return (
    <div className="flex flex-col items-start gap-2">
      <form
        action={formAction}
        className="flex flex-wrap items-center gap-2"
      >
        <input type="hidden" name="doctorId" value={doctorId} />
        <input
          type="text"
          name="reason"
          placeholder="Reason (for rejection)"
          className="h-8 w-full rounded-md border border-border-strong bg-surface px-2 text-xs text-text-primary placeholder:text-text-muted focus:border-azure focus:outline-none sm:w-44"
        />
        <button
          type="submit"
          name="status"
          value="APPROVED"
          disabled={pending}
          className="rounded-md bg-primary-900 px-3 py-1.5 text-xs font-medium text-white hover:bg-primary-800 disabled:opacity-60"
        >
          Approve
        </button>
        <button
          type="submit"
          name="status"
          value="REJECTED"
          disabled={pending}
          className="rounded-md border border-border-strong bg-surface px-3 py-1.5 text-xs font-medium text-text-primary hover:bg-neutral-50 disabled:opacity-60"
        >
          Reject
        </button>
      </form>
      {state ? (
        state.ok ? (
          <SuccessMessage>{state.message}</SuccessMessage>
        ) : (
          <ErrorMessage>{state.message}</ErrorMessage>
        )
      ) : null}
    </div>
  );
}