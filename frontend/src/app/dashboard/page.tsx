import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState, StatCard } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { springJson } from "@/lib/api";

export const metadata: Metadata = { title: "Dashboard" };

// Authenticated, per-user data — not prerenderable.
export const instant = false;

type DonorSummary = {
  totalDonors?: number;
  eligibleDonors?: number;
  pendingRequests?: number;
  matchedRequests?: number;
  byBloodGroup?: Record<string, number>;
};

export default async function DashboardPage() {
  const user = await requireRole(["PATIENT"], "/dashboard");

  // Spring being down should not take the whole shell down with it — fall back
  // to zeros so the page still renders and the user can navigate.
  let summary: DonorSummary = {};
  try {
    summary = await springJson<DonorSummary>("/patient/donor-admin/api/summary");
  } catch {
    summary = {};
  }

  const groups = Object.entries(summary.byBloodGroup ?? {});

  return (
    <AppShell user={user}>
      <PageHeader
        title={`Welcome back, ${user.name?.split(" ")[0] ?? user.email}`}
        description="Here's what's happening in the CloudCare community today."
      />

      <div className="grid grid-cols-4 gap-4">
        <StatCard label="Registered donors" value={summary.totalDonors ?? 0} />
        <StatCard label="Eligible to donate" value={summary.eligibleDonors ?? 0} />
        <StatCard
          label="Pending requests"
          value={summary.pendingRequests ?? 0}
        />
        <StatCard label="Matched requests" value={summary.matchedRequests ?? 0} />
      </div>

      <div className="grid grid-cols-3 gap-4">
        <Card className="col-span-2">
          <CardHeader
            title="Donors by blood group"
            description="Community coverage at a glance."
          />
          <CardBody>
            {groups.length === 0 ? (
              <EmptyState
                title="No donor data yet"
                description="Donor registrations will appear here."
              />
            ) : (
              <ul className="flex flex-col gap-2.5">
                {groups.map(([group, count]) => (
                  <li key={group} className="flex items-center gap-3">
                    <span className="w-20 shrink-0 text-sm font-semibold text-blood">
                      {group}
                    </span>
                    <span className="h-2 flex-1 overflow-hidden rounded-full bg-neutral-100">
                      <span
                        className="block h-full rounded-full bg-primary-500"
                        style={{
                          width: `${Math.min(
                            100,
                            (count / Math.max(...groups.map(([, c]) => c))) * 100,
                          )}%`,
                        }}
                      />
                    </span>
                    <span className="w-8 shrink-0 text-right text-sm text-text-secondary">
                      {count}
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader title="Quick actions" />
          <CardBody className="flex flex-col gap-2">
            <Link
              href="/requests/new"
              className="rounded-md border border-border px-3 py-2 text-sm text-text-primary hover:bg-neutral-100"
            >
              Post a blood request
            </Link>
            <Link
              href="/donors/new"
              className="rounded-md border border-border px-3 py-2 text-sm text-text-primary hover:bg-neutral-100"
            >
              Register as a donor
            </Link>
            <Link
              href="/doctors"
              className="rounded-md border border-border px-3 py-2 text-sm text-text-primary hover:bg-neutral-100"
            >
              Find a doctor
            </Link>
          </CardBody>
        </Card>
      </div>
    </AppShell>
  );
}