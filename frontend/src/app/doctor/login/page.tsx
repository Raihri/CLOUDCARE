import type { Metadata } from "next";

import { PortalLogin } from "@/components/auth/PortalLogin";

export const metadata: Metadata = { title: "Doctor sign in" };

export default function DoctorLoginPage() {
  return (
    <PortalLogin
      role="DOCTOR"
      title="Doctor sign in"
      subtitle="Manage appointments, availability and patient records."
      icon="⚕"
    />
  );
}