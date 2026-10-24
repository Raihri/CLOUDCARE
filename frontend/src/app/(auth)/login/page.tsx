import type { Metadata } from "next";

import { AuthExperience } from "@/components/auth/AuthExperience";

export const metadata: Metadata = { title: "Sign in" };

export const instant = false;

type LoginSearchParams = {
  next?: string;
  registered?: string;
  email?: string;
};

export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<LoginSearchParams>;
}) {
  const { next, registered, email } = await searchParams;

  // Guard redirects put the interrupted path in `next`. Use it to start the
  // generic picker on the right portal instead of always defaulting to patient.
  const initialRole = next?.startsWith("/doctor")
    ? "DOCTOR"
    : next?.startsWith("/admin")
      ? "ADMIN"
      : "PATIENT";

  return (
    <AuthExperience
      initialMode="login"
      initialRole={initialRole}
      next={next}
      initialEmail={email}
      registered={Boolean(registered)}
      forgotHref="/forgot-password"
      showSignup
    />
  );
}
