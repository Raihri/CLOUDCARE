import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody } from "@/components/ui/Card";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { DonorForm } from "./DonorForm";

export const metadata: Metadata = { title: "Register as a Donor" };

export const instant = false;

export default async function NewDonorPage() {
  const user = await requireRole(["PATIENT"], "/donors/new");

  return (
    <AppShell user={user}>
      <PageHeader
        title="Register as a Donor"
        description="Once registered, matching patients can reach you directly."
        action={
          <Link href="/donors">
            <span className="text-sm font-medium text-highlight hover:underline">
              ← Donor list
            </span>
          </Link>
        }
      />

      <div className="max-w-2xl">
        <Card>
          <CardBody>
            <DonorForm defaultEmail={user.email} />
          </CardBody>
        </Card>
      </div>
    </AppShell>
  );
}