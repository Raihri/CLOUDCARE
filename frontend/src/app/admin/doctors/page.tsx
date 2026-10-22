import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchAdminDoctors } from "@/lib/admin-api";
import { DoctorsTable } from "./DoctorsTable";

export const metadata: Metadata = { title: "Doctors List" };

export const instant = false;

export default async function AdminDoctorsPage({
  searchParams,
}: {
  searchParams: Promise<{ status?: string }>;
}) {
  const user = await requireRole(["ADMIN"], "/admin/doctors");
  const { status } = await searchParams;
  const filter = status ?? "ALL";

  const doctors = await fetchAdminDoctors(filter);

  const tabs = [
    { key: "ALL", label: "All" },
    { key: "APPROVED", label: "Approved" },
    { key: "REJECTED", label: "Rejected" },
  ];

  return (
    <AppShell user={user}>
      <PageHeader
        title="Doctors List"
        description="Every doctor who has applied to practise on CloudCare."
      />

      <div className="mb-4 flex gap-2">
        {tabs.map((t) => (
          <a
            key={t.key}
            href={t.key === "ALL" ? "/admin/doctors" : `/admin/doctors?status=${t.key}`}
            className={
              filter === t.key
                ? "rounded-md bg-navy px-3 py-1.5 text-sm font-medium text-white"
                : "rounded-md border border-border-strong bg-surface px-3 py-1.5 text-sm font-medium text-text-primary hover:bg-neutral-50"
            }
          >
            {t.label}
          </a>
        ))}
        <a
          href="/admin/doctors/pending"
          className="ml-2 rounded-md border border-border-strong bg-surface px-3 py-1.5 text-sm font-medium text-highlight hover:bg-neutral-50"
        >
          Pending →
        </a>
      </div>

      <DoctorsTable doctors={doctors} />
    </AppShell>
  );
}