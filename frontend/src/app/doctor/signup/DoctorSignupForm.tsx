"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/ui/Feedback";
import { FormField, Input, Select } from "@/components/ui/Form";
import { applyAsDoctor, type DoctorSignupState } from "./actions";

const SPECIALIZATIONS = [
  "General Medicine",
  "Cardiology",
  "Dermatology",
  "ENT",
  "Gastroenterology",
  "General Surgery",
  "Obstetrics & Gynaecology",
  "Ophthalmology",
  "Orthopaedics",
  "Paediatrics",
  "Psychiatry",
  "Pulmonology",
  "Radiology",
  "Urology",
];

export function DoctorSignupForm() {
  const [state, formAction, pending] = useActionState<DoctorSignupState, FormData>(
    applyAsDoctor,
    undefined,
  );

  return (
    <form action={formAction} className="flex flex-col gap-4">
      <FormField label="Full name" htmlFor="name" required>
        <Input id="name" name="name" autoComplete="name" required />
      </FormField>

      <FormField label="Email" htmlFor="email" required>
        <Input id="email" name="email" type="email" autoComplete="email" required />
      </FormField>

      <FormField
        label="Password"
        htmlFor="password"
        required
        hint="At least 6 characters."
      >
        <Input
          id="password"
          name="password"
          type="password"
          autoComplete="new-password"
          minLength={6}
          required
        />
      </FormField>

      <FormField label="BMDC registration number" htmlFor="bmdcRegNo" required>
        <Input id="bmdcRegNo" name="bmdcRegNo" required />
      </FormField>

      <FormField label="Specialization" htmlFor="specialization">
        <Select id="specialization" name="specialization" defaultValue="">
          <option value="">Select a specialization</option>
          {SPECIALIZATIONS.map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </Select>
      </FormField>

      <FormField
        label="Degrees"
        htmlFor="degrees"
        hint="For example: MBBS, FCPS"
      >
        <Input id="degrees" name="degrees" />
      </FormField>

      <FormField label="Phone number" htmlFor="phoneNumber">
        <Input id="phoneNumber" name="phoneNumber" type="tel" />
      </FormField>

      {/* Both files are required by the Spring endpoint; keep them visible
          rather than hidden so users know exactly what is being uploaded. */}
      <FormField label="Profile photo" htmlFor="profileImageFile" required>
        <input
          id="profileImageFile"
          name="profileImageFile"
          type="file"
          accept="image/*"
          required
          className="w-full rounded-md border border-border-strong bg-surface px-3 py-2 text-sm text-text-secondary file:mr-3 file:rounded file:border-0 file:bg-primary-50 file:px-3 file:py-1.5 file:text-sm file:font-medium file:text-primary-700 hover:file:bg-primary-100"
        />
      </FormField>

      <FormField label="Supporting certificate" htmlFor="certificateFile" required>
        <input
          id="certificateFile"
          name="certificateFile"
          type="file"
          accept="image/*,application/pdf"
          required
          className="w-full rounded-md border border-border-strong bg-surface px-3 py-2 text-sm text-text-secondary file:mr-3 file:rounded file:border-0 file:bg-primary-50 file:px-3 file:py-1.5 file:text-sm file:font-medium file:text-primary-700 hover:file:bg-primary-100"
        />
      </FormField>

      {state?.error ? <ErrorMessage>{state.error}</ErrorMessage> : null}

      <Button type="submit" fullWidth disabled={pending}>
        {pending ? "Submitting…" : "Submit application"}
      </Button>
    </form>
  );
}