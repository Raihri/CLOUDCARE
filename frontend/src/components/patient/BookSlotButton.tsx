"use client";

import { useRouter } from "next/navigation";
import { useState, useTransition } from "react";

import { Button } from "@/components/ui/Button";
import { springFetch } from "@/lib/api-client";

/**
 * Books a slot, then refreshes so the availability list reflects the new state.
 *
 * A refresh is required rather than a redirect: booking flips the slot's
 * `booked` flag server-side, and the availability endpoint filters those out,
 * so the list is correct only after a re-render.
 */
export function BookSlotButton({
  doctorId,
  slotId,
}: {
  doctorId: number;
  slotId: number;
}) {
  const router = useRouter();
  const [pending, startTransition] = useTransition();
  const [error, setError] = useState<string | null>(null);

  function book() {
    setError(null);
    startTransition(async () => {
      try {
        const res = await springFetch(`/patient/api/${doctorId}/book/${slotId}`, {
          method: "POST",
        });

        if (res.ok) {
          router.refresh();
          return;
        }

        // Spring returns {success:false, message} on a rejected booking —
        // most often "That time slot has already been booked."
        const body = await res.json().catch(() => null);
        setError(body?.message ?? "Could not book that slot.");
        router.refresh();
      } catch {
        setError("Could not reach the server. Check your connection.");
      }
    });
  }

  return (
    <div className="flex flex-col items-end gap-1">
      <Button size="sm" disabled={pending} onClick={book}>
        {pending ? "Booking…" : "Book"}
      </Button>
      {error ? <p className="text-xs text-danger">{error}</p> : null}
    </div>
  );
}