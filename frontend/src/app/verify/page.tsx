import type { Metadata } from "next";

import { AuthCard } from "@/components/auth/AuthCard";
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
    <AuthCard
      icon="✉"
      title="Verify your email"
      subtitle={
        email ? (
          <>
            We sent a 6-digit code to{" "}
            <span className="font-medium text-text-secondary">{email}</span>.
          </>
        ) : (
          "Enter the 6-digit code we emailed you."
        )
      }
    >
      <VerifyForm email={email} />
    </AuthCard>
  );
}