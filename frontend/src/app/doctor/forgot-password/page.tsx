import type { Metadata } from "next";

import { AuthCard } from "@/components/auth/AuthCard";
import { DoctorForgotForm } from "./DoctorForgotForm";

export const metadata: Metadata = { title: "Reset your password" };

export default function DoctorForgotPasswordPage() {
  return (
    <AuthCard
      icon={<i className="bx bxs-lock-alt text-xl" />}
      title="Reset your password"
      subtitle="Enter the email on your doctor account."
    >
      <DoctorForgotForm />
    </AuthCard>
  );
}