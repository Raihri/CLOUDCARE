import type { Metadata } from "next";

import { RegisterForm } from "./RegisterForm";

export const metadata: Metadata = { title: "Create your account" };

export default function RegisterPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-surface-sunken px-6 py-12">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center text-center">
          <span className="flex size-11 items-center justify-center rounded-xl bg-primary-600 text-xl text-white">
            <span aria-hidden>❥</span>
          </span>
          <h1 className="mt-4 text-xl font-semibold tracking-tight text-text-primary">
            Create your CloudCare account
          </h1>
          <p className="mt-1 text-sm text-text-muted">
            Donate blood, book appointments and track your health.
          </p>
        </div>

        <div className="rounded-lg border border-border bg-surface p-6 shadow-sm">
          <RegisterForm />
        </div>

        <p className="mt-4 text-center text-sm text-text-muted">
          Already have an account?{" "}
          <a href="/login" className="font-medium text-primary-600 hover:text-primary-700">
            Sign in
          </a>
        </p>
      </div>
    </div>
  );
}