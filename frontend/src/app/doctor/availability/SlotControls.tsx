"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState, ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { FormField, Input } from "@/components/ui/Form";
import { addSlot, removeSlot, type SlotActionState } from "./actions";

export function AddSlotForm() {
  const [state, formAction, pending] = useActionState<SlotActionState, FormData>(
    addSlot,
    undefined,
  );

  return (
    <form action={formAction} className="flex flex-col gap-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <FormField label="Date" htmlFor="date" required>
          <Input id="date" name="date" type="date" required />
        </FormField>
        <FormField label="Start" htmlFor="startTime" required>
          <Input id="startTime" name="startTime" type="time" required />
        </FormField>
        <FormField label="End" htmlFor="endTime" required>
          <Input id="endTime" name="endTime" type="time" required />
        </FormField>
      </div>

      <label className="flex items-center gap-2 text-sm text-text-secondary">
        <input
          type="checkbox"
          name="telemedicine"
          value="true"
          className="h-4 w-4 accent-azure"
        />
        Offer this slot for telemedicine
      </label>

      {state ? (
        state.ok ? (
          <SuccessMessage>{state.message}</SuccessMessage>
        ) : (
          <ErrorMessage>{state.message}</ErrorMessage>
        )
      ) : null}

      <Button type="submit" disabled={pending}>
        {pending ? "Adding…" : "Add slot"}
      </Button>
    </form>
  );
}

export function RemoveSlotButton({
  slotId,
  booked,
}: {
  slotId: number;
  booked: boolean;
}) {
  const [state, formAction, pending] = useActionState<SlotActionState, FormData>(
    removeSlot,
    undefined,
  );

  // A booked slot can't be deleted — the appointment points at it. The backend
  // enforces this too; disabling here just avoids a pointless round-trip.
  if (booked) {
    return <span className="text-xs text-text-muted">Booked</span>;
  }

  return (
    <form action={formAction} className="flex flex-col items-end gap-1">
      <input type="hidden" name="slotId" value={slotId} />
      <button
        type="submit"
        disabled={pending}
        className="rounded-md border border-border-strong bg-surface px-3 py-1.5 text-xs font-medium text-text-primary hover:bg-neutral-50 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {pending ? "…" : "Remove"}
      </button>
      {state && !state.ok ? (
        <span className="text-xs text-danger">{state.message}</span>
      ) : null}
    </form>
  );
}

export function SlotList({
  slots,
}: {
  slots: {
    id: number;
    date: string | null;
    startTime: string | null;
    endTime: string | null;
    booked: boolean;
    telemedicineAvailable: boolean | null;
  }[];
}) {
  if (slots.length === 0) {
    return (
      <Card>
        <CardBody>
          <EmptyState
            icon={<i className="bx bx-time" aria-hidden />}
            title="No slots defined"
            description="Add times above and patients will be able to book them."
          />
        </CardBody>
      </Card>
    );
  }

  return (
    <Card>
      <CardBody>
        <div className="divide-y divide-border">
          {slots.map((s) => (
            <div
              key={s.id}
              className="flex flex-col gap-3 py-3 first:pt-0 last:pb-0 sm:flex-row sm:items-center sm:justify-between"
            >
              <div className="min-w-0">
                <p className="font-medium text-text-primary">
                  {s.date ?? "—"}
                </p>
                <p className="mt-0.5 text-xs text-text-muted">
                  {s.startTime}–{s.endTime} ·{" "}
                  {s.telemedicineAvailable ? "Telemedicine" : "In person"} ·{" "}
                  {s.booked ? "Booked" : "Open"}
                </p>
              </div>
              <div className="shrink-0">
                <RemoveSlotButton slotId={s.id} booked={s.booked} />
              </div>
            </div>
          ))}
        </div>
      </CardBody>
    </Card>
  );
}