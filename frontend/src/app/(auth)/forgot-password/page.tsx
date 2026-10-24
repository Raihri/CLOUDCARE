import type { Metadata } from "next";

import { AuthCard } from "@/components/auth/AuthCard";
import { RequestResetForm } from "@/components/auth/PasswordResetForms";

export const metadata: Metadata = { title: "Reset your password" };

/** Step 1 of the patient reset: request an emailed, single-use link. */
export default function ForgotPasswordPage() {
  return (
    <AuthCard
      icon={<i className="bx bxs-lock-alt text-xl" />}
      title="Reset your password"
      subtitle="Enter your account email and we'll send you a reset link."
    >
      <RequestResetForm role="patient" />
    </AuthCard>
  );
}
