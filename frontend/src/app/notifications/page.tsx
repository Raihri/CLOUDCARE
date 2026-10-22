import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { Badge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchNotifications } from "@/lib/patient-api";

export const metadata: Metadata = { title: "Notifications" };

export const instant = false;

export default async function NotificationsPage() {
  const user = await requireRole(["PATIENT"], "/notifications");
  const notifications = await fetchNotifications();

  const unread = notifications.filter((n) => !n.read).length;

  return (
    <AppShell user={user}>
      <PageHeader
        title="Notifications"
        description={
          unread > 0
            ? `${unread} unread update${unread === 1 ? "" : "s"}.`
            : "You're all caught up."
        }
      />

      {notifications.length === 0 ? (
        <Card>
          <CardBody>
            <EmptyState
              icon={<i className="bx bxs-bell" aria-hidden />}
              title="No notifications"
              description="Updates about your requests, appointments and donors will show up here."
            />
          </CardBody>
        </Card>
      ) : (
        <Card>
          <CardBody className="flex flex-col divide-y divide-border">
            {notifications.map((n) => (
              <div
                key={n.id}
                className="flex items-start gap-3 py-3 first:pt-0 last:pb-0"
              >
                <span
                  className={`mt-0.5 flex size-8 shrink-0 items-center justify-center rounded-full ${
                    n.read
                      ? "bg-neutral-100 text-text-muted"
                      : "bg-primary-50 text-highlight"
                  }`}
                >
                  <i className="bx bxs-bell" aria-hidden />
                </span>

                <div className="min-w-0 flex-1">
                  <p
                    className={
                      n.read
                        ? "text-sm text-text-secondary"
                        : "text-sm font-medium text-text-primary"
                    }
                  >
                    {n.message}
                  </p>
                  {n.date ? (
                    <p className="mt-0.5 text-xs text-text-muted">
                      {n.date.replace("T", " ").slice(0, 16)}
                    </p>
                  ) : null}
                </div>

                {!n.read ? <Badge tone="info">New</Badge> : null}
              </div>
            ))}
          </CardBody>
        </Card>
      )}
    </AppShell>
  );
}