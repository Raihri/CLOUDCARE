import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { BloodGroupBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState, StatCard } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchDonorSummary, fetchUrgentRequests } from "@/lib/admin-api";

export const metadata: Metadata = { title: "Donor Analysis" };

export const instant = false;

export default async function AdminDonorsPage() {
  const user = await requireRole(["ADMIN"], "/admin/donors");
  const [summary, urgent] = await Promise.all([
    fetchDonorSummary(),
    fetchUrgentRequests(),
  ]);

  const byGroup = summary?.byBloodGroup ?? {};
  const groups = Object.entries(byGroup).sort(([, a], [, b]) => b - a);
  const max = groups.length > 0 ? Math.max(...groups.map(([, c]) => c)) : 0;

  return (
    <AppShell user={user}>
      <PageHeader
        title="Donor Analysis"
        description="Supply by blood group, and the requests that need attention first."
      />

      <div className="grid grid-cols-4 gap-4">
        <StatCard
          label="Total donors"
          value={summary?.totalDonors ?? "—"}
          icon={<i className="bx bxs-droplet" aria-hidden />}
        />
        <StatCard
          label="Eligible now"
          value={summary?.eligibleDonors ?? "—"}
          icon={<i className="bx bxs-check-circle" aria-hidden />}
        />
        <StatCard
          label="Pending requests"
          value={summary?.pendingRequests ?? "—"}
          icon={<i className="bx bxs-time" aria-hidden />}
        />
        <StatCard
          label="Matched"
          value={summary?.matchedRequests ?? "—"}
          icon={<i className="bx bxs-link" aria-hidden />}
        />
      </div>

      <div className="mt-5 grid grid-cols-2 gap-5">
        <Card>
          <CardBody>
            <h2 className="mb-4 text-base font-semibold text-text-primary">
              Supply by blood group
            </h2>
            {groups.length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-bar-chart-alt-2" aria-hidden />}
                title="No donors registered"
                description="Groups appear here as people sign up."
              />
            ) : (
              <ul className="space-y-2">
                {groups.map(([group, count]) => (
                  <li key={group} className="flex items-center gap-3">
                    <span className="w-12 shrink-0">
                      <BloodGroupBadge group={group} />
                    </span>
                    <div className="h-2 flex-1 overflow-hidden rounded-full bg-neutral-200">
                      <div
                        className="h-full rounded-full bg-blood"
                        style={{ width: `${max ? (count / max) * 100 : 0}%` }}
                      />
                    </div>
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
          <CardBody>
            <h2 className="mb-4 text-base font-semibold text-text-primary">
              Urgent requests
            </h2>
            {urgent.length === 0 ? (
              <EmptyState
                icon={<i className="bx bxs-check-circle" aria-hidden />}
                title="Nothing urgent"
                description="No pending requests due within three days."
              />
            ) : (
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-border text-xs uppercase tracking-wide text-text-muted">
                    <th className="pb-3 pr-4 font-medium">Patient</th>
                    <th className="pb-3 pr-4 font-medium">Group</th>
                    <th className="pb-3 pr-4 font-medium">Units</th>
                    <th className="pb-3 font-medium">Needed by</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {urgent.map((r) => (
                    <tr key={r.id}>
                      <td className="py-3 pr-4 text-text-primary">
                        {r.name ?? "Anonymous"}
                      </td>
                      <td className="py-3 pr-4">
                        <BloodGroupBadge group={r.bloodGroup} />
                      </td>
                      <td className="py-3 pr-4 text-text-secondary">
                        {r.units ?? "—"}
                      </td>
                      <td className="py-3 text-xs font-medium text-danger">
                        {r.requiredDate ?? "No date"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </CardBody>
        </Card>
      </div>
    </AppShell>
  );
}