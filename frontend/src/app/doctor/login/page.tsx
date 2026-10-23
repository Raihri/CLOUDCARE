import type { Metadata } from "next";

import { PortalLogin } from "@/components/auth/PortalLogin";

export const metadata: Metadata = { title: "Doctor sign in" };

export const instant = false;

type DoctorLoginSearchParams = {
  next?: string;
};

export default async function DoctorLoginPage({
  searchParams,
}: {
  searchParams: Promise<DoctorLoginSearchParams>;
}) {
  const { next } = await searchParams;

  return (
    <PortalLogin
      role="DOCTOR"
      next={next}
      forgotHref="/doctor/forgot-password"
      signupHref="/doctor/signup"
    />
  );
}
