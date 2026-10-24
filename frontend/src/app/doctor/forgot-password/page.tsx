import type { Metadata } from "next";

import { AuthCard } from "@/components/auth/AuthCard";
import { RequestResetForm } from "@/components/auth/PasswordResetForms";

export const metadata: Metadata = { title: "Reset your password" };

/** Step 1 of the doctor reset: request an emailed, single-use link. */
export default function DoctorForgotPasswordPage() {
  return (
    <AuthCard
      icon={<i className="bx bxs-lock-alt text-xl" />}
      title="Reset your password"
      subtitle="Enter the email on your doctor account."
    >
      <RequestResetForm role="doctor" />
    </AuthCard>
  );
}
