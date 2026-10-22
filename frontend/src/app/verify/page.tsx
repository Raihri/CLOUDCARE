import type { Metadata } from "next";

import { VerifyForm } from "./VerifyForm";

export const metadata: Metadata = { title: "Verify your email" };

/** Reads searchParams for the email hint, so it can't be fully prerendered. */
export const instant = false;

export default async function VerifyPage({
  searchParams,
}: {
  searchParams: Promise<{ email?: string }>;
}) {
  const { email = "" } = await searchParams;

  return (
    <div className="flex min-h-screen items-center justify-center bg-surface-sunken px-6 py-12">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center text-center">
          <span className="flex size-11 items-center justify-center rounded-xl bg-primary-600 text-xl text-white">
            <span aria-hidden>✉</span>
          </span>
          <h1 className="mt-4 text-xl font-semibold tracking-tight text-text-primary">
            Verify your email
          </h1>
          <p className="mt-1 text-sm text-text-muted">
            {email ? (
              <>
                We sent a 6-digit code to <span className="font-medium text-text-secondary">{email}</span>.
              </>
            ) : (
              "Enter the 6-digit code we emailed you."
            )}
          </p>
        </div>

        <div className="rounded-lg border border-border bg-surface p-6 shadow-sm">
          <VerifyForm email={email} />
        </div>
      </div>
    </div>
  );
}