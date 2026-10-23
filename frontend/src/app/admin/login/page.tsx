import type { Metadata } from "next";

import { PortalLogin } from "@/components/auth/PortalLogin";

export const metadata: Metadata = { title: "Admin sign in" };

export const instant = false;

type AdminLoginSearchParams = {
  next?: string;
};

export default async function AdminLoginPage({
  searchParams,
}: {
  searchParams: Promise<AdminLoginSearchParams>;
}) {
  const { next } = await searchParams;

  return <PortalLogin role="ADMIN" next={next} />;
}
