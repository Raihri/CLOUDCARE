import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody } from "@/components/ui/Card";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { ChangePasswordForm } from "./ChangePasswordForm";

export const metadata: Metadata = { title: "Change password" };

export const instant = false;

export default async function AdminChangePasswordPage() {
  const user = await requireRole(["ADMIN"], "/admin/change-password");

  return (
    <AppShell user={user}>
      <PageHeader
        title="Change password"
        description="Update the password for your administrator account."
      />

      <Card className="max-w-lg">
        <CardBody>
          <ChangePasswordForm />
        </CardBody>
      </Card>
    </AppShell>
  );
}