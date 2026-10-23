import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchAdminDashboard, fetchAdminDoctors } from "@/lib/admin-api";
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

  const [doctors, stats] = await Promise.all([
    fetchAdminDoctors(filter),
    fetchAdminDashboard(),
  ]);

  const tabs = [
    { key: "ALL", label: "All", count: stats?.totalDoctors },
    { key: "APPROVED", label: "Approved", count: stats?.approvedDoctors },
    { key: "REJECTED", label: "Rejected", count: stats?.rejectedDoctors },
  ];

  return (
    <AppShell user={user}>
      <PageHeader
        title="Doctors List"
        description="Every doctor who has applied to practise on CloudCare."
      />

      <div className="flex flex-wrap items-center gap-3">
        <div className="inline-flex flex-wrap gap-1 rounded-xl border border-border bg-surface p-1 shadow-sm">
          {tabs.map((tab) => {
            const active = filter === tab.key;
            return (
              <Link
                key={tab.key}
                href={
                  tab.key === "ALL"
                    ? "/admin/doctors"
                    : `/admin/doctors?status=${tab.key}`
                }
                aria-current={active ? "page" : undefined}
                className={[
                  "inline-flex items-center gap-1.5 rounded-lg px-3.5 py-1.5 text-sm font-medium transition-colors",
                  active
                    ? "bg-primary-900 text-white shadow-sm"
                    : "text-text-secondary hover:bg-neutral-100 hover:text-text-primary",
                ].join(" ")}
              >
                {tab.label}
                {tab.count !== undefined ? (
                  <span
                    className={[
                      "rounded-full px-1.5 py-0.5 text-[10px] font-semibold tabular-nums",
                      active
                        ? "bg-white/15 text-white"
                        : "bg-neutral-100 text-text-muted",
                    ].join(" ")}
                  >
                    {tab.count}
                  </span>
                ) : null}
              </Link>
            );
          })}
        </div>

        <Link
          href="/admin/doctors/pending"
          className="ml-auto inline-flex items-center gap-2 rounded-lg border border-primary-200 bg-primary-50 px-3.5 py-2 text-sm font-medium text-primary-700 transition-colors hover:bg-primary-100"
        >
          <i aria-hidden className="bx bxs-time text-base leading-none" />
          Pending applications
          {stats?.pendingDoctors ? (
            <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-primary-600 px-1.5 text-[10px] font-semibold tabular-nums text-white">
              {stats.pendingDoctors}
            </span>
          ) : null}
        </Link>
      </div>

      <DoctorsTable doctors={doctors} />
    </AppShell>
  );
}
