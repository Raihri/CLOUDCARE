import type { Metadata } from "next";

import { PortalLogin } from "@/components/auth/PortalLogin";

export const metadata: Metadata = { title: "Admin sign in" };

export default function AdminLoginPage() {
  return (
    <PortalLogin
      role="ADMIN"
      title="Administrator sign in"
      subtitle="Approve doctors, review donors and manage feedback."
      icon="◈"
    />
  );
}