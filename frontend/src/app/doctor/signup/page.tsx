import type { Metadata } from "next";
import Link from "next/link";

import { AuthCard } from "@/components/auth/AuthCard";
import { DoctorSignupForm } from "./DoctorSignupForm";

export const metadata: Metadata = { title: "Apply as a doctor" };

export default function DoctorSignupPage() {
  return (
    <AuthCard
      wide
      icon={<i className="bx bxs-first-aid" aria-hidden />}
      title="Apply as a doctor"
      subtitle="An administrator reviews every application before you can sign in."
      footer={
        <p className="mt-4 text-center text-sm text-text-muted">
          <Link
            href="/"
            className="font-medium text-text-secondary transition-colors hover:text-primary-600"
          >
            ← Back to CloudCare
          </Link>
        </p>
      }
    >
      <DoctorSignupForm />
    </AuthCard>
  );
}
