import type { Metadata } from "next";

import { AuthCard } from "@/components/auth/AuthCard";
import { ChooseNewPasswordForm } from "@/components/auth/PasswordResetForms";

export const metadata: Metadata = { title: "Choose a new password" };

/** Reads ?token= off the emailed link, so it can't be prerendered. */
export const instant = false;

/** Step 2 of the patient reset: the page the emailed link opens. */
export default async function ResetPasswordPage({
  searchParams,
}: {
  searchParams: Promise<{ token?: string }>;
}) {
  const { token = "" } = await searchParams;

  return (
    <AuthCard icon={<i className="bx bxs-lock-alt text-xl" />} title="Choose a new password">
      <ChooseNewPasswordForm role="patient" token={token} />
    </AuthCard>
  );
}
