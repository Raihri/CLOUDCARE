import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchAdminDoctors } from "@/lib/admin-api";
import { DoctorsTable } from "../DoctorsTable";

export const metadata: Metadata = { title: "Pending Doctors" };

export const instant = false;

export default async function AdminPendingDoctorsPage() {
  const user = await requireRole(["ADMIN"], "/admin/doctors/pending");
  const doctors = await fetchAdminDoctors("PENDING");

  return (
    <AppShell user={user}>
      <PageHeader
        title="Pending Applications"
        description="Approve or reject each application. Rejections keep the record so the applicant can see why."
        action={
          <Link
            href="/admin/doctors"
            className="text-sm font-medium text-highlight hover:underline"
          >
            ← All doctors
          </Link>
        }
      />

      <DoctorsTable doctors={doctors} />
    </AppShell>
  );
}