import type { Metadata } from "next";
import Link from "next/link";

import { AppShell } from "@/components/layout/AppShell";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { requireRole } from "@/lib/auth";
import { fetchNotifications, type PatientNotification } from "@/lib/patient-api";

export const metadata: Metadata = { title: "Notifications" };

export const instant = false;

/* ------------------------------------------------------------ formatting */

/**
 * Notifications arrive as one flat sentence with no category field — the DTO
 * carries only id/message/date/read. Rather than ask the backend for a new
 * field, we infer a category from the wording.
 *
 * ORDER MATTERS: `find` returns the first match, so the most specific
 * categories come first. "doctor" and "donat" both occur in appointment and
 * request messages respectively, and putting request first would swallow
 * anything like "confirm your donation appointment". Each pattern is therefore
 * narrow on purpose, and the catch-all sits last so an unrecognised message
 * degrades to "general" instead of vanishing.
 */
type Category = {
  key: string;
  label: string;
  icon: string;
  test: RegExp;
  href: string;
};

const CATEGORIES: Category[] = [
  {
    key: "appointment",
    label: "Appointments",
    icon: "bxs-calendar-check",
    test: /\b(appointment|slot|booking|booked|reschedul\w*|consultation)\b/i,
    href: "/appointments",
  },
  {
    key: "prescription",
    label: "Prescriptions",
    icon: "bxs-capsule",
    test: /\b(prescription|follow[- ]?up|medication|medicine|dose)\b/i,
    href: "/prescriptions",
  },
  {
    key: "request",
    label: "Blood requests",
    icon: "bxs-droplet",
    test: /\b(request|donat\w+|donor|blood|matched|unit)\b/i,
    href: "/requests",
  },
  {
    key: "profile",
    label: "Profile & records",
    icon: "bxs-user",
    test: /\b(profile|record|verif\w+|approv\w+|reject\w+|regist\w+|password|sign[- ]?up)\b/i,
    href: "/health-records",
  },
  {
    key: "general",
    label: "General",
    icon: "bxs-bell",
    test: /.*/,
    href: "/dashboard",
  },
];

function categorise(n: PatientNotification): Category {
  const text = n.message ?? "";
  return CATEGORIES.find((c) => c.test.test(text)) ?? CATEGORIES[CATEGORIES.length - 1];
}

/* --------------------------------------------------------------- relative time */

/**
 * Parse a notification timestamp.
 *
 * The backend emits `Date.toString()`, e.g.
 *   "Fri Oct 23 2026 10:59:04 GMT+0600 (Bangladesh Standard Time)"
 * That already parses correctly in V8 — and MUST NOT be pre-processed. An
 * earlier version did `.replace(" ", "T")` to normalise an ISO-ish string and
 * corrupted the weekday instead, so every timestamp silently failed to parse
 * and the page rendered no times at all.
 *
 * ISO strings are accepted too, in case the backend ever switches, since
 * `new Date(iso)` handles those natively.
 */
function parseDate(raw: string | null): Date | null {
  if (!raw) return null;
  const d = new Date(raw);
  return Number.isNaN(d.getTime()) ? null : d;
}

/**
 * "4m ago" rather than "2026-10-23 04:12:00".
 *
 * Relative time is shorter and honest about the precision we actually have —
 * the backend's string carries a timezone abbreviation but no formal offset,
 * so an absolute timestamp would over-promise.
 */
function relativeTime(raw: string | null): string {
  const then = parseDate(raw);
  if (!then) return "";

  const seconds = Math.round((Date.now() - then.getTime()) / 1000);
  // Clamped: clock skew between the JVM and the browser can put a notification
  // slightly in the future, and "-3m ago" is worse than "just now".
  if (seconds < 60) return "just now";

  const minutes = Math.round(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;

  const hours = Math.round(minutes / 60);
  if (hours < 24) return `${hours}h ago`;

  const days = Math.round(hours / 24);
  if (days < 7) return `${days}d ago`;

  return then.toLocaleDateString("en-GB", {
    day: "numeric",
    month: "short",
    year: then.getFullYear() === new Date().getFullYear() ? undefined : "numeric",
  });
}

/** Day heading for the grouped timeline. */
function dayLabel(raw: string | null): string {
  const d = parseDate(raw);
  if (!d) return "Earlier";

  const today = new Date();
  const startOf = (x: Date) => new Date(x.getFullYear(), x.getMonth(), x.getDate()).getTime();
  const diffDays = Math.round((startOf(today) - startOf(d)) / 86400000);

  if (diffDays <= 0) return "Today";
  if (diffDays === 1) return "Yesterday";
  if (diffDays < 7) return "Earlier this week";
  return d.toLocaleDateString("en-GB", { day: "numeric", month: "long" });
}

/* ----------------------------------------------------------------- render */

export default async function NotificationsPage() {
  const user = await requireRole(["PATIENT"], "/notifications");
  const notifications = await fetchNotifications();

  const unread = notifications.filter((n) => !n.read).length;

  // Newest first. The endpoint's ordering is whatever the entity list happens
  // to be, so we sort rather than trust it. Undated rows sink to the bottom
  // instead of scrambling the timeline.
  const sorted = [...notifications].sort((a, b) => {
    const ta = parseDate(a.date)?.getTime() ?? NaN;
    const tb = parseDate(b.date)?.getTime() ?? NaN;
    if (Number.isNaN(ta) && Number.isNaN(tb)) return 0;
    if (Number.isNaN(ta)) return 1;
    if (Number.isNaN(tb)) return -1;
    return tb - ta;
  });

  // Group into day buckets for the timeline.
  const buckets: { day: string; items: PatientNotification[] }[] = [];
  for (const n of sorted) {
    const day = dayLabel(n.date);
    const last = buckets[buckets.length - 1];
    if (last && last.day === day) last.items.push(n);
    else buckets.push({ day, items: [n] });
  }

  // Per-category totals for the summary strip.
  const categoryCounts = new Map<string, number>();
  for (const n of sorted) {
    const k = categorise(n).key;
    categoryCounts.set(k, (categoryCounts.get(k) ?? 0) + 1);
  }
  const presentCategories = CATEGORIES.filter((c) => categoryCounts.has(c.key));

  return (
    <AppShell user={user} counts={{ unreadNotifications: unread }}>
      {/* ---- Header ---- */}
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-text-primary">
            Notifications
          </h1>
          <p className="mt-1 text-sm text-text-secondary">
            {unread > 0
              ? `${unread} unread update${unread === 1 ? "" : "s"} — everything here has happened since your last visit.`
              : "You're all caught up. Nothing new since your last visit."}
          </p>
        </div>

        {notifications.length > 0 ? (
          <div className="flex items-center gap-2">
            <span className="rounded-full bg-surface px-3 py-1.5 text-xs font-medium text-text-secondary shadow-sm">
              {notifications.length} total
            </span>
            <span
              className={[
                "rounded-full px-3 py-1.5 text-xs font-semibold",
                unread > 0
                  ? "bg-blood-soft text-blood-dark"
                  : "bg-success-soft text-success",
              ].join(" ")}
            >
              {unread > 0 ? `${unread} unread` : "All read"}
            </span>
          </div>
        ) : null}
      </div>

      {notifications.length === 0 ? (
        <Card>
          <EmptyState
            icon={<i className="bx bxs-bell" aria-hidden />}
            title="Nothing here yet"
            description="Updates about your requests, appointments and donors will appear here as they happen."
            action={
              <Link
                href="/dashboard"
                className="rounded-md bg-primary-900 px-4 py-2 text-sm font-medium text-white hover:bg-primary-800"
              >
                Back to dashboard
              </Link>
            }
          />
        </Card>
      ) : (
        <div className="flex flex-col gap-5">
          {/* ---- Category breakdown ----
              Gives the list a shape before you read a single line, so a
              patient with 30 notifications can see at a glance that it's
              mostly request traffic rather than 30 separate things. */}
          {presentCategories.length > 1 ? (
            <Card className="px-5 py-4">
              <div className="flex flex-wrap items-center gap-x-6 gap-y-3">
                {presentCategories.map((c) => (
                  <Link
                    key={c.key}
                    href={c.href}
                    className="group flex items-center gap-2.5 no-underline"
                  >
                    <span className="flex size-8 items-center justify-center rounded-lg bg-primary-50 text-highlight transition-colors group-hover:bg-primary-100">
                      <i className={`bx ${c.icon} text-lg`} aria-hidden />
                    </span>
                    <span>
                      <span className="block text-sm font-medium text-text-primary">
                        {c.label}
                      </span>
                      <span className="block text-xs text-text-muted">
                        {categoryCounts.get(c.key)} update
                        {categoryCounts.get(c.key) === 1 ? "" : "s"}
                      </span>
                    </span>
                  </Link>
                ))}
              </div>
            </Card>
          ) : null}

          {/* ---- Timeline ---- */}
          {buckets.map((bucket) => (
            <section key={bucket.day}>
              <div className="mb-2.5 flex items-center gap-3">
                <h2 className="text-xs font-semibold tracking-wide text-text-secondary uppercase">
                  {bucket.day}
                </h2>
                <span className="h-px flex-1 bg-border" aria-hidden />
              </div>

              <Card>
                <ul className="divide-y divide-border">
                  {bucket.items.map((n) => {
                    const cat = categorise(n);
                    return (
                      <li key={n.id}>
                        <Link
                          href={cat.href}
                          className={[
                            "flex items-start gap-4 px-5 py-4 no-underline transition-colors hover:bg-neutral-50",
                            // Unread rows get a faint tint so they're findable
                            // without relying on the "New" badge alone.
                            n.read ? "" : "bg-primary-50/40",
                          ].join(" ")}
                        >
                          <span
                            className={[
                              "flex size-9 shrink-0 items-center justify-center rounded-full",
                              n.read
                                ? "bg-neutral-100 text-text-muted"
                                : "bg-primary-100 text-primary-700",
                            ].join(" ")}
                            aria-hidden
                          >
                            <i className={`bx ${cat.icon} text-lg`} />
                          </span>

                          <span className="min-w-0 flex-1">
                            <span
                              className={[
                                "block text-sm",
                                n.read
                                  ? "text-text-secondary"
                                  : "font-medium text-text-primary",
                              ].join(" ")}
                            >
                              {n.message}
                            </span>
                            <span className="mt-1 flex items-center gap-2 text-xs text-text-muted">
                              <span>{cat.label}</span>
                              <span aria-hidden>·</span>
                              <span>{relativeTime(n.date)}</span>
                            </span>
                          </span>

                          {!n.read ? (
                            <Badge tone="info">New</Badge>
                          ) : null}
                        </Link>
                      </li>
                    );
                  })}
                </ul>
              </Card>
            </section>
          ))}

          {/* ---- What to do next ---- */}
          <Card className="px-5 py-4">
            <p className="text-sm font-medium text-text-primary">
              Looking for something specific?
            </p>
            <div className="mt-3 flex flex-wrap gap-2">
              {[
                { href: "/requests", label: "Blood requests", icon: "bxs-droplet" },
                { href: "/appointments", label: "Appointments", icon: "bxs-calendar-check" },
                { href: "/prescriptions", label: "Prescriptions", icon: "bxs-capsule" },
                { href: "/donors", label: "Find a donor", icon: "bx-search-alt" },
              ].map((link) => (
                <Link
                  key={link.href}
                  href={link.href}
                  className="inline-flex items-center gap-2 rounded-md border border-border px-3.5 py-2 text-sm text-text-primary transition-colors hover:border-primary-600 hover:bg-primary-50"
                >
                  <i className={`bx ${link.icon} text-lg text-highlight`} aria-hidden />
                  {link.label}
                </Link>
              ))}
            </div>
          </Card>
        </div>
      )}
    </AppShell>
  );
}