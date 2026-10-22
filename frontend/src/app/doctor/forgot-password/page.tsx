import type { Metadata } from "next";

import { DoctorForgotForm } from "./DoctorForgotForm";

export const metadata: Metadata = { title: "Reset your password" };

export default function DoctorForgotPasswordPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-surface-sunken px-6 py-12">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center text-center">
          <span className="flex size-11 items-center justify-center rounded-xl bg-primary-600 text-white">
            <i className="bx bxs-lock-alt text-xl" aria-hidden />
          </span>
          <h1 className="mt-4 text-xl font-semibold tracking-tight text-text-primary">
            Reset your password
          </h1>
          <p className="mt-1 text-sm text-text-muted">
            Enter the email on your doctor account.
          </p>
        </div>

        <div className="rounded-lg border border-border bg-surface p-6 shadow-sm">
          <DoctorForgotForm />
        </div>
      </div>
    </div>
  );
}