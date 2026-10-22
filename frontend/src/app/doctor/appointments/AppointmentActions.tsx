"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { setAppointmentStatus, type ActionState } from "../actions";

/**
 * Confirm/cancel controls for one appointment.
 *
 * Each row gets its own useActionState so one row's pending state doesn't
 * disable every button in the table.
 */
export function AppointmentActions({
  appointmentId,
  status,
}: {
  appointmentId: number;
  status: string | null;
}) {
  const [state, formAction, pending] = useActionState<ActionState, FormData>(
    setAppointmentStatus,
    undefined,
  );

  // A cancelled appointment is terminal — offering "confirm" on it would let a
  // doctor reopen a slot the patient may already have rebooked elsewhere.
  if (status === "CANCELLED") {
    return (
      <span className="text-xs text-text-muted">
        {state ? (
          state.ok ? (
            <SuccessMessage>{state.message}</SuccessMessage>
          ) : (
            <ErrorMessage>{state.message}</ErrorMessage>
          )
        ) : null}
      </span>
    );
  }

  return (
    <div className="flex flex-col items-end gap-2">
      <form action={formAction} className="flex gap-2">
        <input type="hidden" name="appointmentId" value={appointmentId} />
        {status !== "CONFIRMED" ? (
          <button
            type="submit"
            name="action"
            value="confirm"
            disabled={pending}
            className="rounded-md bg-primary-900 px-3 py-1.5 text-xs font-medium text-white hover:bg-primary-800 disabled:cursor-not-allowed disabled:opacity-60"
          >
            Confirm
          </button>
        ) : null}
        <button
          type="submit"
          name="action"
          value="cancel"
          disabled={pending}
          className="rounded-md border border-border-strong bg-surface px-3 py-1.5 text-xs font-medium text-text-primary hover:bg-neutral-50 disabled:cursor-not-allowed disabled:opacity-60"
        >
          Cancel
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