import type { Metadata } from "next";

import { AuthCard } from "@/components/auth/AuthCard";
import { DoctorSignupForm } from "./DoctorSignupForm";

export const metadata: Metadata = { title: "Apply as a doctor" };

export default function DoctorSignupPage() {
  return (
    <AuthCard
      wide
      icon="⚕"
      title="Apply as a doctor"
      subtitle="An administrator reviews every application before you can sign in."
    >
      <DoctorSignupForm />
    </AuthCard>
  );
}