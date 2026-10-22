import type { Metadata } from "next";

import { AuthCard } from "@/components/auth/AuthCard";
import { RegisterForm } from "./RegisterForm";

export const metadata: Metadata = { title: "Create your account" };

export default function RegisterPage() {
  return (
    <AuthCard
      icon="❥"
      title="Create your CloudCare account"
      subtitle="Donate blood, book appointments and track your health."
      footer={
        <p className="mt-4 text-center text-sm text-text-muted">
          Already have an account?{" "}
          <a href="/login" className="font-medium text-primary-600 hover:text-primary-700">
            Sign in
          </a>
        </p>
      }
    >
      <RegisterForm />
    </AuthCard>
  );
}