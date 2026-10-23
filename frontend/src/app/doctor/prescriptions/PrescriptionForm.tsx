"use client";

import { useActionState, useEffect, useRef } from "react";

import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { FormField, Input, Textarea } from "@/components/ui/Form";
import { createPrescription, type PrescriptionState } from "./actions";

function todayIso(): string {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60_000);
  return local.toISOString().slice(0, 10);
}

/**
 * Prescription entry form.
 *
 * Medicines are free text (one per line is the convention shown in the
 * placeholder) because the demo has no formulary. The follow-up date is
 * required: the reminder job and the patient's prescriptions page both key off
 * it, and a prescription without a next visit would silently never remind
 * anyone.
 */
export function PrescriptionForm({
  patientId,
  patientName,
}: {
  patientId: number;
  patientName: string | null;
}) {
  const [state, formAction, pending] = useActionState<
    PrescriptionState,
    FormData
  >(createPrescription, undefined);

  const formRef = useRef<HTMLFormElement>(null);

  // Clear the clinical fields after a successful save so the doctor can write
  // the next prescription without manually deleting the previous one. The
  // hidden patient id is restored by the browser's default values.
  useEffect(() => {
    if (state?.ok) {
      formRef.current?.reset();
    }
  }, [state]);

  return (
    <Card>
      <CardHeader
        title="Write a prescription"
        description={
          patientName
            ? `For ${patientName} · patient ID ${patientId}`
            : `For patient ID ${patientId}`
        }
      />
      <CardBody>
        <form ref={formRef} action={formAction} className="flex flex-col gap-4">
          <input type="hidden" name="patientId" value={patientId} />

          <FormField
            label="Diagnosis / reason for visit"
            htmlFor="prescription-diagnosis"
            hint="Optional. A short working diagnosis is enough."
          >
            <Input
              id="prescription-diagnosis"
              name="diagnosis"
              placeholder="e.g. Hypertension, follow-up"
            />
          </FormField>

          <FormField
            label="Medicines & treatment"
            htmlFor="prescription-medicines"
            required
            hint="One medicine per line, with dose and frequency."
          >
            <Textarea
              id="prescription-medicines"
              name="medicines"
              required
              rows={5}
              placeholder={"1. Amlodipine 5 mg — once daily, 30 days\n2. Aspirin 75 mg — once daily after food"}
            />
          </FormField>

          <FormField
            label="Instructions for the patient"
            htmlFor="prescription-instructions"
            hint="Optional. Diet, rest, tests to bring, warning signs, etc."
          >
            <Textarea
              id="prescription-instructions"
              name="instructions"
              rows={3}
              placeholder="e.g. Check blood pressure twice a week and bring the readings."
            />
          </FormField>

          <FormField
            label="Follow-up date"
            htmlFor="prescription-follow-up"
            required
            hint="The patient is notified when this date is near and when it arrives."
          >
            <Input
              id="prescription-follow-up"
              name="followUpDate"
              type="date"
              min={todayIso()}
              required
            />
          </FormField>

          {state ? (
            state.ok ? (
              <SuccessMessage>{state.message}</SuccessMessage>
            ) : (
              <ErrorMessage>{state.message}</ErrorMessage>
            )
          ) : null}

          <div className="flex justify-end">
            <Button type="submit" disabled={pending}>
              {pending ? "Saving…" : "Save prescription"}
            </Button>
          </div>
        </form>
      </CardBody>
    </Card>
  );
}
