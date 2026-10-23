"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Form";

/**
 * Patient-ID lookup for the prescription maker.
 *
 * The ID is the same numeric key shown on the patient's chart and in
 * appointment rows. The lookup lives in the URL (?patientId=...) so the page
 * stays server-rendered and the doctor can refresh without losing context.
 */
export function PrescriptionPatientSearch({
  defaultPatientId = "",
}: {
  defaultPatientId?: string;
}) {
  const router = useRouter();
  const [patientId, setPatientId] = useState(defaultPatientId);
  const [error, setError] = useState("");

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmed = patientId.trim();

    if (!/^\d+$/.test(trimmed)) {
      setError("Enter a numeric patient ID.");
      return;
    }

    setError("");
    router.push(`/doctor/prescriptions?patientId=${trimmed}`);
  }

  return (
    <form onSubmit={onSubmit}>
      <label
        htmlFor="prescription-patient-id"
        className="text-sm font-medium text-text-primary"
      >
        Find a patient by ID
      </label>
      <p className="mt-0.5 text-xs text-text-muted">
        Enter the patient ID to see their details, previous prescriptions and
        write the next one.
      </p>

      <div className="mt-3 flex flex-col gap-2 sm:flex-row">
        <Input
          id="prescription-patient-id"
          name="patientId"
          inputMode="numeric"
          pattern="[0-9]*"
          placeholder="e.g. 292800"
          value={patientId}
          onChange={(event) => setPatientId(event.target.value)}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? "prescription-patient-id-error" : undefined}
        />
        <Button type="submit" className="shrink-0">
          Open patient
        </Button>
      </div>

      {error ? (
        <p
          id="prescription-patient-id-error"
          role="alert"
          className="mt-2 text-xs text-danger"
        >
          {error}
        </p>
      ) : null}
    </form>
  );
}
