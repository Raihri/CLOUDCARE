"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";

import { BrandLogo } from "@/components/brand/BrandLogo";
import { isActive, NAV_BY_ROLE } from "@/components/layout/nav";
import { LiveNotifications } from "@/components/notifications/LiveNotifications";
import type { CurrentUser } from "@/lib/auth";

const ROLE_LABEL: Record<CurrentUser["role"], string> = {
  PATIENT: "Patient",
  DOCTOR: "Doctor",
  ADMIN: "Administrator",
};

const PORTAL_LABEL: Record<CurrentUser["role"], string> = {
  PATIENT: "Patient portal",
  DOCTOR: "Doctor portal",
  ADMIN: "Admin portal",
};

/**
 * App shell: sidebar + topbar.
 *
 * The panel is a single dark card inset from the viewport edge, with one
 * consistent 12px gutter either side of it. Every nav row shares the same
 * height, icon column and text baseline, so icons line up down the full list
 * and labels start on one vertical rule regardless of glyph or word length.
 */
export function AppShell({
  user,
  counts,
  children,
}: {
  user: CurrentUser;
  /** Per-item badge counts, keyed by `NavItem.badge`. Passed down from the page
   *  (which already fetched the data) so the sidebar can show e.g. unread
   *  notifications on every page, not just the notifications page. */
  counts?: Record<string, number>;
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const items = NAV_BY_ROLE[user.role];
  const [menuOpen, setMenuOpen] = useState(false);

  // Escape closes the drawer, matching every other dismissible surface, and the
  // page behind it stays put while the drawer is open.
  useEffect(() => {
    if (!menuOpen) return;

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") setMenuOpen(false);
    };
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    window.addEventListener("keydown", onKeyDown);
    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", onKeyDown);
    };
  }, [menuOpen]);

  const displayName = user.name ?? user.email;
  const initial = displayName.charAt(0).toUpperCase();

  return (
    <div className="min-h-screen">
      {/* Mobile backdrop. Hidden on desktop, where the sidebar is always open. */}
      {menuOpen ? (
        <button
          type="button"
          aria-label="Close navigation"
          onClick={() => setMenuOpen(false)}
          className="fixed inset-0 z-40 cursor-default bg-neutral-900/50 backdrop-blur-[2px] lg:hidden"
        />
      ) : null}

      {/* ---- Sidebar ----
          `inset-y-3 left-3` gives the panel the same 12px breathing room on
          every edge as the main content has. The 272px panel plus the 16px
          gutter on each side puts the content rail at 304px. */}
      <aside
        id="app-sidebar"
        className={[
          "fixed inset-y-3 left-3 z-50 flex w-[272px] flex-col overflow-hidden rounded-[20px] bg-sidebar",
          "shadow-2xl shadow-neutral-900/25 ring-1 ring-white/5",
          "transition-transform duration-300 ease-out will-change-transform",
          "lg:inset-y-4 lg:left-4 lg:translate-x-0",
          menuOpen ? "translate-x-0" : "-translate-x-[115%]",
        ].join(" ")}
      >
        {/* Brand — left-aligned with the nav, so the wordmark, menu heading and
            menu labels all share one optical left edge. */}
        <div className="flex shrink-0 items-center gap-3 px-5 pt-6 pb-5">
          <Link
            href="/"
            aria-label="CloudCare portal hub"
            title="Back to the portal hub"
            className="flex min-w-0 flex-1 items-center gap-3 transition-opacity hover:opacity-90"
          >
            <BrandLogo
              size={40}
              priority
              className="shadow-lg shadow-primary-900/40 ring-1 ring-white/10"
            />
            <div className="min-w-0">
              <p className="truncate text-[17px] font-semibold leading-tight tracking-[-0.01em] text-white">
                CloudCare
              </p>
              <p className="mt-0.5 truncate text-[10px] font-medium uppercase tracking-[0.16em] text-white/40">
                {PORTAL_LABEL[user.role]}
              </p>
            </div>
          </Link>
          <button
            type="button"
            aria-label="Close navigation"
            onClick={() => setMenuOpen(false)}
            className="ml-auto flex size-8 shrink-0 items-center justify-center rounded-lg text-white/50 transition-colors hover:bg-white/5 hover:text-white lg:hidden"
          >
            <i className="bx bx-x text-xl leading-none" aria-hidden />
          </button>
        </div>

        <div className="mx-5 h-px shrink-0 bg-white/[0.07]" />

        <p className="shrink-0 px-5 pt-5 pb-2 text-[10px] font-semibold uppercase tracking-[0.18em] text-white/35">
          Main menu
        </p>

        {/* Nav — `min-h-0` lets the list shrink and scroll instead of pushing
            the profile card out of the panel on short windows. The scrollbar
            itself is hidden so the dark card keeps a clean right edge. */}
        <nav
          aria-label="Primary"
          className="scrollbar-hidden min-h-0 flex-1 overflow-x-hidden overflow-y-auto px-3 pb-4"
        >
          <ul className="space-y-1">
            {items.map((item) => {
              const active = isActive(item, pathname);
              const count = item.badge ? (counts?.[item.badge] ?? 0) : 0;

              return (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    aria-current={active ? "page" : undefined}
                    onClick={() => setMenuOpen(false)}
                    className={[
                      "group relative flex h-11 items-center gap-3 rounded-xl px-3 text-sm no-underline",
                      "transition-colors duration-200",
                      active
                        ? "bg-white/[0.08] font-medium text-white"
                        : "font-normal text-white/65 hover:bg-white/[0.05] hover:text-white",
                    ].join(" ")}
                  >
                    {/* One 3px cyan bar, vertically centred, marks the active
                        route without shifting the row's content. */}
                    {active ? (
                      <span
                        aria-hidden
                        className="absolute top-1/2 left-0 h-5 w-[3px] -translate-y-1/2 rounded-r-full bg-highlight"
                      />
                    ) : null}

                    {/* Fixed 20px icon cell: every Boxicon centre sits on the
                        same vertical line, and every label starts 12px after
                        it. This is what keeps the list from looking ragged. */}
                    <i
                      aria-hidden
                      className={[
                        "bx flex size-5 shrink-0 items-center justify-center text-[20px] leading-none",
                        item.icon,
                        active
                          ? "text-highlight"
                          : "text-white/40 group-hover:text-white/75",
                      ].join(" ")}
                    />

                    <span className="truncate">{item.label}</span>

                    {count > 0 ? (
                      <>
                        <span
                          aria-hidden
                          className="ml-auto flex h-5 min-w-5 shrink-0 items-center justify-center rounded-full bg-highlight px-1.5 text-[10px] font-semibold tabular-nums text-sidebar"
                        >
                          {count > 99 ? "99+" : count}
                        </span>
                        {/* Announced as part of the link name, not as a stray
                            label on a decorative span. */}
                        <span className="sr-only">{count} unread</span>
                      </>
                    ) : null}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        {user.role === "PATIENT" ? (
          <div className="shrink-0 px-3 pb-2">
            <LiveNotifications email={user.email} />
          </div>
        ) : null}

        {/* Account footer. The avatar and name sit on one line, with sign-out
            as a peer row below — both use the same 20px icon column as the nav. */}
        <div className="shrink-0 border-t border-white/[0.07] p-3">
          <div className="flex items-center gap-3 px-2 py-2">
            <span className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-sidebar-sub text-sm font-semibold text-white ring-1 ring-white/10">
              {initial}
            </span>
            <div className="min-w-0 flex-1">
              <p
                className="truncate text-[13px] font-medium text-white"
                title={displayName}
              >
                {displayName}
              </p>
              <p className="mt-0.5 truncate text-[10px] font-medium uppercase tracking-[0.14em] text-white/40">
                {ROLE_LABEL[user.role]}
              </p>
            </div>
          </div>

          <form action="/api/logout" method="post" className="mt-1">
            <button
              type="submit"
              className="flex h-9 w-full items-center gap-3 rounded-lg px-3 text-[13px] font-medium text-white/50 transition-colors hover:bg-white/5 hover:text-white"
            >
              <i
                aria-hidden
                className="bx bx-log-out flex size-5 shrink-0 items-center justify-center text-[19px] leading-none"
              />
              Sign out
            </button>
          </form>
        </div>
      </aside>

      {/* ---- Content rail ---- */}
      <div className="min-h-screen lg:pl-[304px]">
        {/* Compact mobile bar; desktop keeps the full sidebar instead. */}
        <header className="sticky top-0 z-30 flex h-14 items-center gap-3 border-b border-neutral-200 bg-surface/95 px-4 backdrop-blur lg:hidden">
          <button
            type="button"
            aria-label="Open navigation"
            aria-controls="app-sidebar"
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen(true)}
            className="-ml-1 flex size-9 items-center justify-center rounded-lg text-neutral-700 transition-colors hover:bg-neutral-100"
          >
            <i className="bx bx-menu text-2xl leading-none" aria-hidden />
          </button>
          <Link
            href="/"
            aria-label="CloudCare portal hub"
            title="Back to the portal hub"
            className="flex items-center gap-2.5 transition-opacity hover:opacity-90"
          >
            <BrandLogo
              size={30}
              priority
              className="rounded-lg ring-1 ring-border"
            />
            <span className="text-sm font-semibold text-neutral-800">
              CloudCare
            </span>
          </Link>
        </header>

        <main className="min-h-screen bg-neutral-50">
          <div className="flex flex-col gap-6 px-4 py-6 sm:px-6 lg:px-8">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}
