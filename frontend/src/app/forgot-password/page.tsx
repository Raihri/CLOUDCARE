import type { Metadata } from "next";

import { AuthCard } from "@/components/auth/AuthCard";
import { ForgotForm } from "./ForgotForm";

export const metadata: Metadata = { title: "Reset your password" };

/**
 * Replaces forgot_password.html, otp_reset.html and reset_password.html as a
 * single three-step flow.
 *
 * Shares the signed-out shell with the three login portals, so entering this
 * flow looks like it never left the login page.
 */
export default function ForgotPasswordPage() {
  return (
    <AuthCard
      icon={<i className="bx bxs-lock-alt text-xl" />}
      title="Reset your password"
      subtitle="We'll email you a verification code to confirm it's you."
    >
      <ForgotForm />
    </AuthCard>
  );
}