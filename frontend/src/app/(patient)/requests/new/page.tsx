import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody } from "@/components/ui/Card";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { RequestForm } from "./RequestForm";

export const metadata: Metadata = { title: "Post a Blood Request" };

export const instant = false;

export default async function NewRequestPage() {
  const user = await requireRole(["PATIENT"], "/requests/new");

  return (
    <AppShell user={user}>
      <PageHeader
        title="Post a Blood Request"
        description="Tell us what you need and we'll look for eligible donors."
        action={
          <Link href="/requests">
            <span className="text-sm font-medium text-highlight hover:underline">
              ← My requests
            </span>
          </Link>
        }
      />

      <div className="max-w-2xl">
        <Card>
          <CardBody>
            <RequestForm defaultEmail={user.email} />
          </CardBody>
        </Card>
      </div>
    </AppShell>
  );
}