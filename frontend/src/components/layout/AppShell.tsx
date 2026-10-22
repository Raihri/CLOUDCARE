"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { Button } from "@/components/ui/Button";
import { isActive, NAV_BY_ROLE } from "@/components/layout/nav";
import type { CurrentUser } from "@/lib/auth";

/**
 * App shell: sidebar + topbar. Replaces the 17 hand-rolled `<div class="sidebar">`
 * blocks.
 *
 * Visual values are ported verbatim from static/css/style.css:
 *   .sidebar            fixed 260px, #23282D, border-radius 15px, margin 5px
 *   .sidebar li:hover   background #191E23, 4px transparent left border
 *   .sidebar li.active  border-left-color #00A0D2, icon+label #00A0D2
 *   .nav-links li i     20px icon, 78px column, 50px row height
 *   .profile-details    #000000C2, radius 10px, 52px avatar, radius 16px
 *   .home-section       left 260px, background #F7F8FA
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
    <div className="min-h-screen">
      {/* ---- Sidebar (.sidebar) ---- */}
      <aside className="fixed inset-y-0 left-0 z-100 flex w-[260px] flex-col rounded-xl bg-sidebar">
        {/* Logo section */}
        <div className="mt-6 flex flex-col items-center justify-center px-4 pb-2">
          <span className="flex size-16 items-center justify-center rounded-2xl bg-primary-600 text-2xl font-semibold text-white">
            C
          </span>
          <span className="mt-1 text-xl font-semibold tracking-wider text-white">
            CloudCare
          </span>
        </div>

        {/* Nav links */}
        <nav className="mt-4 flex-1 overflow-y-auto overflow-x-hidden pb-8">
          <ul>
            {items.map((item) => {
              const active = isActive(item, pathname);
              return (
                <li
                  key={item.href}
                  className={[
                    "border-l-4 transition-colors duration-300",
                    active
                      ? "border-highlight bg-sidebar-hover"
                      : "border-transparent hover:bg-sidebar-hover",
                  ].join(" ")}
                >
                  <Link
                    href={item.href}
                    aria-current={active ? "page" : undefined}
                    className="flex items-center no-underline"
                  >
                    <i
                      className={`bx ${item.icon} flex h-[50px] w-[78px] shrink-0 items-center justify-center text-xl`}
                      style={{
                        color: active ? "#00a0d2" : "#fff",
                      }}
                      aria-hidden
                    />
                    <span
                      className={`text-sm transition-colors duration-300 ${
                        active ? "font-medium text-highlight" : "text-white"
                      }`}
                    >
                      {item.label}
                    </span>
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        {/* Profile details */}
        <div className="absolute inset-x-0 bottom-0 px-3 pb-3">
          <div className="flex items-center justify-between rounded-[10px] bg-sidebar-footer px-3 py-3">
            <div className="flex min-w-0 items-center gap-3">
              <span className="flex size-[52px] shrink-0 items-center justify-center rounded-2xl bg-sidebar-sub text-lg font-semibold text-white">
                {(user.name ?? user.email).charAt(0).toUpperCase()}
              </span>
              <div className="min-w-0">
                <p className="truncate text-sm font-medium text-white">
                  {user.name ?? user.email}
                </p>
                <p className="truncate text-xs text-white/60">{user.role}</p>
              </div>
            </div>
          </div>
          <form action="/api/logout" method="post" className="mt-2">
            <Button
              variant="ghost"
              size="sm"
              fullWidth
              type="submit"
              className="text-white hover:bg-sidebar-hover"
            >
              Sign out
            </Button>
          </form>
        </div>
      </aside>

      {/* ---- Home section (.home-section) ---- */}
      <div className="min-h-screen pl-[260px]">
        <main className="min-h-screen bg-neutral-50 p-3">
          {children}
        </main>
      </div>
    </div>
  );
}