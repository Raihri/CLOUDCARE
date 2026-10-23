import type { Metadata } from "next";

import { AuthExperience } from "@/components/auth/AuthExperience";

export const metadata: Metadata = { title: "Create your account" };

export default function RegisterPage() {
  return (
    <AuthExperience
      initialMode="signup"
      forgotHref="/forgot-password"
      showSignup
    />
  );
}
