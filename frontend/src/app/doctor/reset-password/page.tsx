import type { Metadata } from "next";

import { DoctorForgotForm } from "../forgot-password/DoctorForgotForm";

export const metadata: Metadata = { title: "Choose a new password" };

/** Reads ?token= off the emailed link, so it can't be prerendered. */
export const instant = false;

/**
 * Target of the link in the doctor's reset email.
 *
 * Kept as its own route because that URL is already baked into emails sent by
 * the legacy controller; changing it would break links that are in inboxes.
 */
export default async function DoctorResetPasswordPage({
  searchParams,
}: {
  searchParams: Promise<{ token?: string }>;
}) {
  const { token = "" } = await searchParams;

  return (
    <div className="flex min-h-screen items-center justify-center bg-surface-sunken px-6 py-12">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center text-center">
          <span className="flex size-11 items-center justify-center rounded-xl bg-primary-600 text-white">
            <i className="bx bxs-lock-alt text-xl" aria-hidden />
          </span>
          <h1 className="mt-4 text-xl font-semibold tracking-tight text-text-primary">
            Choose a new password
          </h1>
        </div>

        <div className="rounded-lg border border-border bg-surface p-6 shadow-sm">
          {/* The shared form renders the password step whenever a token is
              present, and the "resend" step when it isn't — which is exactly
              the behaviour a clicked-but-expired link needs. */}
          <DoctorForgotForm initialToken={token} />
        </div>
      </div>
    </div>
  );
}