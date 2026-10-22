import type { Metadata } from "next";

import { AuthCard } from "@/components/auth/AuthCard";
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
    <AuthCard
      icon={<i className="bx bxs-lock-alt text-xl" />}
      title="Choose a new password"
    >
      {/* The shared form renders the password step whenever a token is
          present, and the "resend" step when it isn't — which is exactly
          the behaviour a clicked-but-expired link needs. */}
      <DoctorForgotForm initialToken={token} />
    </AuthCard>
  );
}