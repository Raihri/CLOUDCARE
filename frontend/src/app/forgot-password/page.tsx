import type { Metadata } from "next";

import { AuthCard } from "@/components/auth/AuthCard";
import { ForgotForm } from "./ForgotForm";

export const metadata: Metadata = { title: "Reset your password" };

/**
 * One-step password reset. The demo build intentionally skips email OTP so a
 * reviewer can reset an account without access to the project's inbox.
 */
export default function ForgotPasswordPage() {
  return (
    <AuthCard
      icon={<i className="bx bxs-lock-alt text-xl" />}
      title="Reset your password"
      subtitle="Enter your email and choose a new password."
    >
      <ForgotForm />
    </AuthCard>
  );
}