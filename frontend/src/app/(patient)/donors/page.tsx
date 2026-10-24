import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { BloodGroupBadge, StatusBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState, ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchDonors } from "@/lib/patient-api";

export const metadata: Metadata = { title: "Find a Donor" };

export const instant = false;

export default async function DonorsPage({
  searchParams,
}: {
  searchParams: Promise<{ registered?: string; eligible?: string; reasons?: string }>;
}) {
  const user = await requireRole(["PATIENT"], "/donors");
  const donors = await fetchDonors();
  const { registered, eligible, reasons } = await searchParams;

  // Donors are a shared public resource, so everyone is listed. Sorting by
  // blood group makes the directory scannable.
  const sorted = [...donors].sort((a, b) =>
    (a.bloodGroup ?? "").localeCompare(b.bloodGroup ?? ""),
  );

  return (
    <AppShell user={user}>
      <PageHeader
        title="Find Donor"
        description="Registered donors, sorted by blood group."
        action={
          <Link
            href="/donors/new"
            className="rounded-md bg-blood px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-blood-dark"
          >
            Register as a donor
          </Link>
        }
      />

      {registered ? (
        <div className="mb-4">
          {eligible === "1" ? (
            <SuccessMessage>
              You&apos;re registered and currently eligible to donate. Thank you
              for helping the community.
            </SuccessMessage>
          ) : (
            <ErrorMessage>
              You&apos;re registered, but you&apos;re not eligible to donate right
              now{reasons ? `: ${reasons.replace(/;/g, ", ")}` : "."} You can
              update your details once that changes.
            </ErrorMessage>
          )}
        </div>
      ) : null}

      {sorted.length === 0 ? (
        <Card>
          <CardBody>
            <EmptyState
              icon={<i className="bx bxs-droplet" aria-hidden />}
              title="No donors registered yet"
              description="Be the first to register as a donor and help someone in need."
              action={
                <Link
                  href="/donors/new"
                  className="rounded-md bg-blood px-4 py-2 text-sm font-medium text-white hover:bg-blood-dark"
                >
                  Register as a donor
                </Link>
              }
            />
          </CardBody>
        </Card>
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {sorted.map((donor) => (
            <div
              key={donor.id}
              className="rounded-lg border border-border bg-surface p-5 shadow-sm"
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <h2 className="truncate font-semibold text-text-primary">
                    {donor.name}
                  </h2>
                  <p className="text-sm text-text-muted">
                    {donor.age} yrs
                    {donor.gender ? ` · ${donor.gender}` : ""}
                  </p>
                </div>
                <BloodGroupBadge group={donor.bloodGroup} />
              </div>

              <dl className="mt-4 flex flex-col gap-2 border-t border-border pt-3 text-sm">
                <div className="flex items-center justify-between gap-2">
                  <dt className="text-text-muted">Location</dt>
                  <dd className="truncate text-right text-text-primary">
                    {[donor.district, donor.thana].filter(Boolean).join(", ") ||
                      "—"}
                  </dd>
                </div>
                <div className="flex items-center justify-between gap-2">
                  <dt className="text-text-muted">Contact</dt>
                  <dd className="text-text-primary">{donor.contact ?? "—"}</dd>
                </div>
                <div className="flex items-center justify-between gap-2">
                  <dt className="text-text-muted">Last donated</dt>
                  <dd className="text-text-primary">
                    {donor.lastDonated ?? "—"}
                  </dd>
                </div>
              </dl>

              <div className="mt-3">
                <StatusBadge status={donor.status} />
              </div>
            </div>
          ))}
        </div>
      )}
    </AppShell>
  );
}