"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Form";

/**
 * Patient-ID lookup used on the doctor dashboard and the patient list.
 *
 * The ID is the same numeric key shown on the patient's profile and in
 * appointment rows. Keeping the lookup client-side means the URL changes only
 * after a valid numeric ID has been entered.
 */
export function PatientSearch({
  autoFocus = false,
  className = "",
}: {
  autoFocus?: boolean;
  className?: string;
}) {
  const router = useRouter();
  const [patientId, setPatientId] = useState("");
  const [error, setError] = useState("");

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmed = patientId.trim();

    if (!/^\d+$/.test(trimmed)) {
      setError("Enter a numeric patient ID.");
      return;
    }

    setError("");
    router.push(`/doctor/patients/${trimmed}`);
  }

  return (
    <form onSubmit={onSubmit} className={className}>
      <label
        htmlFor="patient-id-search"
        className="text-sm font-medium text-text-primary"
      >
        Find a patient by ID
      </label>
      <p className="mt-0.5 text-xs text-text-muted">
        Look up a chart, review the full record and update it in place.
      </p>

      <div className="mt-3 flex flex-col gap-2 sm:flex-row">
        <Input
          id="patient-id-search"
          name="patientId"
          inputMode="numeric"
          pattern="[0-9]*"
          placeholder="e.g. 292800"
          value={patientId}
          onChange={(event) => setPatientId(event.target.value)}
          autoFocus={autoFocus}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? "patient-id-error" : undefined}
        />
        <Button type="submit" className="shrink-0">
          Search patient
        </Button>
      </div>

      {error ? (
        <p id="patient-id-error" role="alert" className="mt-2 text-xs text-danger">
          {error}
        </p>
      ) : null}
    </form>
  );
}
