import type { Metadata } from "next";

import { DoctorSignupForm } from "./DoctorSignupForm";

export const metadata: Metadata = { title: "Apply as a doctor" };

export default function DoctorSignupPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-surface-sunken px-6 py-12">
      <div className="w-full max-w-lg">
        <div className="mb-6 flex flex-col items-center text-center">
          <span className="flex size-11 items-center justify-center rounded-xl bg-primary-600 text-xl text-white">
            <span aria-hidden>⚕</span>
          </span>
          <h1 className="mt-4 text-xl font-semibold tracking-tight text-text-primary">
            Apply as a doctor
          </h1>
          <p className="mt-1 text-sm text-text-muted">
            An administrator reviews every application before you can sign in.
          </p>
        </div>

        <div className="rounded-lg border border-border bg-surface p-6 shadow-sm">
          <DoctorSignupForm />
        </div>
      </div>
    </div>
  );
}