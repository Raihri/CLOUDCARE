"use client";

import Link from "next/link";
import { useActionState, useState } from "react";

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

function SectionHeading({
  icon,
  title,
  description,
}: {
  icon: string;
  title: string;
  description: string;
}) {
  return (
    <div className="mb-4 flex items-start gap-3">
      <span className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-700">
        <i aria-hidden className={`bx ${icon} text-lg leading-none`} />
      </span>
      <div>
        <h2 className="text-sm font-semibold text-text-primary">{title}</h2>
        <p className="mt-0.5 text-xs text-text-muted">{description}</p>
      </div>
    </div>
  );
}

function FileField({
  id,
  name,
  label,
  hint,
  accept,
  fileName,
  onFileChange,
}: {
  id: string;
  name: string;
  label: string;
  hint: string;
  accept: string;
  fileName: string;
  onFileChange: (name: string) => void;
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <span className="text-sm font-medium text-text-primary">
        {label}
        <span className="ml-0.5 text-danger">*</span>
      </span>

      <label
        htmlFor={id}
        className="group flex min-h-36 cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-border-strong bg-neutral-50 px-4 py-5 text-center transition-colors hover:border-primary-400 hover:bg-primary-50/50 focus-within:border-primary-500 focus-within:ring-2 focus-within:ring-primary-100"
      >
        <span
          className={[
            "flex size-10 items-center justify-center rounded-full bg-surface shadow-sm",
            fileName ? "text-success" : "text-primary-600",
          ].join(" ")}
        >
          <i
            aria-hidden
            className={`bx ${fileName ? "bxs-check-circle" : "bx-cloud-upload"} text-xl leading-none`}
          />
        </span>

        <span className="w-full truncate text-sm font-medium text-text-primary">
          {fileName || "Click to upload"}
        </span>
        <span className="text-[11px] leading-snug text-text-muted">
          {fileName ? "Selected — click to change" : hint}
        </span>

        <input
          id={id}
          name={name}
          type="file"
          accept={accept}
          required
          onChange={(event) =>
            onFileChange(event.target.files?.[0]?.name ?? "")
          }
          className="sr-only"
        />
      </label>
    </div>
  );
}

export function DoctorSignupForm() {
  const [state, formAction, pending] = useActionState<
    DoctorSignupState,
    FormData
  >(applyAsDoctor, undefined);
  const [profileName, setProfileName] = useState("");
  const [certificateName, setCertificateName] = useState("");

  if (state?.success) {
    return (
      <div className="flex flex-col items-center px-2 py-8 text-center">
        <span className="flex size-16 items-center justify-center rounded-2xl bg-success-soft text-success">
          <i aria-hidden className="bx bxs-check-circle text-4xl leading-none" />
        </span>
        <h2 className="mt-5 text-xl font-semibold tracking-tight text-text-primary">
          Application submitted
        </h2>
        <p className="mt-2 max-w-md text-sm leading-relaxed text-text-secondary">
          Thank you. An administrator will review your BMDC number and
          certificate. Once approved, you can sign in with the email and
          password you just created.
        </p>

        <div className="mt-6 flex w-full max-w-sm flex-col gap-2">
          <Link
            href="/doctor/login"
            className="inline-flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-primary-900 px-4 text-sm font-medium text-white shadow-sm transition-colors hover:bg-primary-800"
          >
            Go to doctor sign in
            <i aria-hidden className="bx bx-right-arrow-alt text-lg" />
          </Link>
          <Link
            href="/"
            className="inline-flex h-10 w-full items-center justify-center rounded-lg text-sm font-medium text-primary-600 transition-colors hover:bg-primary-50 hover:text-primary-700"
          >
            Back to portal hub
          </Link>
        </div>
      </div>
    );
  }

  return (
    <form action={formAction} className="flex flex-col gap-7">
      <div className="flex gap-3 rounded-xl border border-primary-100 bg-primary-50/70 p-4">
        <span className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-surface text-primary-700 shadow-sm">
          <i aria-hidden className="bx bx-info-circle text-lg leading-none" />
        </span>
        <div>
          <p className="text-sm font-medium text-text-primary">
            Have your documents ready
          </p>
          <p className="mt-0.5 text-xs leading-relaxed text-text-secondary">
            You&apos;ll need your BMDC registration number, a profile photo and
            a supporting certificate. Every application is reviewed by an admin
            before the account is activated.
          </p>
        </div>
      </div>

      <section>
        <SectionHeading
          icon="bxs-user-account"
          title="Account details"
          description="You'll use these to sign in after approval."
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <FormField label="Full name" htmlFor="name" required>
              <Input id="name" name="name" autoComplete="name" required />
            </FormField>
          </div>
          <FormField label="Email" htmlFor="email" required>
            <Input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              required
            />
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
        </div>
      </section>

      <div className="h-px bg-border" />

      <section>
        <SectionHeading
          icon="bxs-briefcase-alt-2"
          title="Professional details"
          description="Shown on your public doctor profile."
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField
            label="BMDC registration number"
            htmlFor="bmdcRegNo"
            required
          >
            <Input id="bmdcRegNo" name="bmdcRegNo" required />
          </FormField>

          <FormField label="Specialization" htmlFor="specialization">
            <Select id="specialization" name="specialization" defaultValue="">
              <option value="">Select a specialization</option>
              {SPECIALIZATIONS.map((specialization) => (
                <option key={specialization} value={specialization}>
                  {specialization}
                </option>
              ))}
            </Select>
          </FormField>

          <FormField
            label="Degrees"
            htmlFor="degrees"
            hint="For example: MBBS, FCPS"
          >
            <Input id="degrees" name="degrees" placeholder="MBBS, FCPS" />
          </FormField>

          <FormField label="Phone number" htmlFor="phoneNumber">
            <Input
              id="phoneNumber"
              name="phoneNumber"
              type="tel"
              autoComplete="tel"
              placeholder="01XXXXXXXXX"
            />
          </FormField>
        </div>
      </section>

      <div className="h-px bg-border" />

      <section>
        <SectionHeading
          icon="bxs-cloud-upload"
          title="Verification documents"
          description="Used only to verify your medical registration."
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <FileField
            id="profileImageFile"
            name="profileImageFile"
            label="Profile photo"
            hint="JPG or PNG, a clear headshot"
            accept="image/*"
            fileName={profileName}
            onFileChange={setProfileName}
          />
          <FileField
            id="certificateFile"
            name="certificateFile"
            label="Supporting certificate"
            hint="BMDC certificate or proof, PDF or image"
            accept="image/*,application/pdf"
            fileName={certificateName}
            onFileChange={setCertificateName}
          />
        </div>
      </section>

      {state?.error ? <ErrorMessage>{state.error}</ErrorMessage> : null}

      <div className="flex flex-col-reverse gap-3 border-t border-border pt-5 sm:flex-row sm:items-center sm:justify-between">
        <Link
          href="/doctor/login"
          className="text-center text-sm font-medium text-text-secondary transition-colors hover:text-primary-600"
        >
          Already approved? Sign in →
        </Link>
        <Button
          type="submit"
          disabled={pending}
          className="w-full sm:w-auto"
        >
          {pending ? "Submitting application…" : "Submit application"}
        </Button>
      </div>
    </form>
  );
}
