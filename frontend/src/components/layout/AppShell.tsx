"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { Button } from "@/components/ui/Button";
import { isActive, NAV_BY_ROLE } from "@/components/layout/nav";
import type { CurrentUser } from "@/lib/auth";

/**
 * App shell: sidebar + topbar. Replaces the 17 hand-rolled `<div class="sidebar">`
 * blocks, each of which had its own markup, icons and active-state logic.
 */
export function AppShell({
  user,
  children,
}: {
  user: CurrentUser;
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const items = NAV_BY_ROLE[user.role];

  return (
    <div className="flex min-h-screen">
      {/* ---- Sidebar ---- */}
      <aside className="w-60 shrink-0 flex flex-col border-r border-border bg-surface">
        <div className="flex h-14 items-center gap-2 border-b border-border px-5">
          <span className="flex size-7 items-center justify-center rounded-md bg-primary-600 text-sm font-bold text-white">
            C
          </span>
          <span className="font-semibold tracking-tight text-text-primary">
            CloudCare
          </span>
        </div>

        <nav className="flex-1 overflow-y-auto p-3">
          <ul className="flex flex-col gap-0.5">
            {items.map((item) => {
              const active = isActive(item, pathname);
              return (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    aria-current={active ? "page" : undefined}
                    className={[
                      "flex items-center gap-3 rounded-md px-3 py-2 text-sm",
                      "transition-colors",
                      active
                        ? "bg-primary-50 font-medium text-primary-700"
                        : "text-text-secondary hover:bg-neutral-100 hover:text-text-primary",
                    ].join(" ")}
                  >
                    <span
                      aria-hidden
                      className={[
                        "w-4 text-center",
                        active ? "text-primary-600" : "text-text-muted",
                      ].join(" ")}
                    >
                      {item.icon}
                    </span>
                    {item.label}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        {/* ---- Signed-in user ---- */}
        <div className="border-t border-border p-3">
          <div className="flex items-center gap-3 rounded-md px-2 py-2">
            <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-primary-100 text-xs font-semibold text-primary-700">
              {(user.name ?? user.email).charAt(0).toUpperCase()}
            </span>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium text-text-primary">
                {user.name ?? user.email}
              </p>
              <p className="truncate text-xs text-text-muted">{user.role}</p>
            </div>
          </div>
          <form action="/api/logout" method="post" className="mt-1">
            <Button variant="ghost" size="sm" fullWidth type="submit">
              Sign out
            </Button>
          </form>
        </div>
      </aside>

      {/* ---- Main ---- */}
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-14 shrink-0 items-center justify-between border-b border-border bg-surface px-6">
          <p className="text-sm text-text-muted">
            {items.find((i) => isActive(i, pathname))?.label ?? "CloudCare"}
          </p>
          <span className="text-sm text-text-secondary">{user.email}</span>
        </header>

        <main className="flex-1 bg-surface-sunken p-6">
          <div className="mx-auto flex max-w-6xl flex-col gap-6">{children}</div>
        </main>
      </div>
    </div>
  );
}