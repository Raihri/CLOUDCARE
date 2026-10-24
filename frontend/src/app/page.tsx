import Link from "next/link";
import { Suspense } from "react";
import type { CSSProperties } from "react";

import { BrandLogo } from "@/components/brand/BrandLogo";
import { RolePortals } from "@/components/marketing/RolePortals";
import { SplineScene } from "@/components/marketing/SplineScene";
import { Button } from "@/components/ui/Button";
import { getCurrentUser, homeFor } from "@/lib/auth";

/** Header auth controls. Session read, so it streams in behind <Suspense>. */
async function AuthActions() {
  const user = await getCurrentUser();

  if (user) {
    const initial = (user.name ?? user.email).charAt(0).toUpperCase();

    return (
      <div className="flex items-center gap-1.5 sm:gap-2">
        <Link
          href={homeFor(user.role)}
          className="hidden items-center gap-2 rounded-full border border-border bg-surface py-1 pr-3.5 pl-1 transition-colors hover:border-primary-300 hover:bg-primary-50 sm:flex"
        >
          <span className="flex size-7 items-center justify-center rounded-full bg-primary-600 text-xs font-semibold text-white">
            {initial}
          </span>
          <span className="max-w-40 truncate text-xs font-medium text-text-primary">
            {user.name ?? user.email}
          </span>
        </Link>

        <Link href={homeFor(user.role)}>
          <Button size="sm">Dashboard</Button>
        </Link>

        <form action="/api/logout" method="post">
          <Button
            type="submit"
            variant="ghost"
            size="sm"
            aria-label="Sign out"
            title="Sign out"
          >
            <i aria-hidden className="bx bx-log-out text-lg leading-none" />
            <span className="hidden sm:inline">Sign out</span>
          </Button>
        </form>
      </div>
    );
  }

  return (
    <div className="flex items-center gap-1.5 sm:gap-2">
      <Link href="/login">
        <Button variant="ghost" size="sm">
          Sign in
        </Button>
      </Link>
      <Link href="/register">
        <Button size="sm">
          <span className="sm:hidden">Sign up</span>
          <span className="hidden sm:inline">Create account</span>
        </Button>
      </Link>
    </div>
  );
}

function AuthActionsSkeleton() {
  return (
    <div className="flex items-center gap-2">
      <div className="h-8 w-16 animate-pulse rounded-md bg-neutral-200" />
      <div className="h-8 w-28 animate-pulse rounded-md bg-neutral-200" />
    </div>
  );
}

/**
 * Matches the portal list's real height (3 rows ≈ 100px each) so the page
 * doesn't jump when the session resolves. The old skeleton drew three 22rem
 * cards, which is ~3x taller than the thing it stood in for.
 */
function PortalSkeleton() {
  return (
    <div aria-busy="true" aria-label="Loading portals" className="border-t border-border">
      {[0, 1, 2].map((i) => (
        <div key={i} className="flex items-center gap-8 border-b border-border py-7">
          <div className="h-7 w-28 shrink-0 animate-pulse rounded bg-neutral-200" />
          <div className="hidden flex-1 animate-pulse rounded bg-neutral-200/60 sm:block sm:h-4" />
          <div className="h-11 w-28 shrink-0 animate-pulse rounded-lg bg-neutral-200" />
        </div>
      ))}
    </div>
  );
}

/**
 * A single edge-docked portal button for the hero. Icon-first, label
 * underneath — compact enough to sit on the scene's edge without
 * competing with the headline, with a frosted chip behind the icon so it
 * reads clearly over both a light sky and a darker part of the scene.
 */
function HeroPortalButton({
  href,
  icon,
  label,
}: {
  href: string;
  icon: string;
  label: string;
}) {
  return (
    <Link
      href={href}
      className="group glass-panel flex w-20 flex-col items-center gap-1.5 rounded-2xl px-2 py-3 shadow-md transition-all hover:-translate-x-0.5 hover:shadow-lg active:scale-[0.97]"
    >
      <span className="flex size-10 items-center justify-center rounded-full bg-primary-900 text-white shadow-sm transition-colors group-hover:bg-primary-800">
        <i aria-hidden className={`bx ${icon} text-lg leading-none`} />
      </span>
      <span className="text-[11px] font-medium text-text-primary">{label}</span>
    </Link>
  );
}

export default function HomePage() {
  return (
    <div className="flex min-h-screen flex-col bg-neutral-100">
      {/* ---- Header ---- */}
      <header className="border-b border-border bg-surface">
        <div className="mx-auto flex h-16 w-full max-w-5xl items-center gap-3 px-5 sm:px-8">
          <Link href="/" className="flex items-center gap-2.5">
            <BrandLogo size={36} priority className="ring-1 ring-border shadow-sm" />
            <span className="text-[17px] font-semibold tracking-tight text-text-primary">
              CloudCare
            </span>
          </Link>

          {/* `ml-auto` belongs on this wrapper, not on the individual actions:
              putting it here keeps the whole auth group pinned right instead
              of spreading the buttons across the header. */}
          <div className="ml-auto">
            <Suspense fallback={<AuthActionsSkeleton />}>
              <AuthActions />
            </Suspense>
          </div>
        </div>
      </header>

      <main className="flex-1">
        {/*
          Hero.

          There used to be a dark banner here reading "One health platform.
          Three ways in." followed 100px later by a light section headed
          "Choose your portal". Two headlines saying the same thing, in
          different styles, is the single clearest "generated landing page"
          tell — so the second one is gone and the hero is now the only
          headline before the list.

          No blur orbs, no badge pill, no gradient. Type and whitespace only.
        */}
        <section className="relative overflow-hidden bg-surface lg:min-h-[38rem]">
          <div className="ambient-wash" aria-hidden />

          {/* The scene now fills the whole hero as a backdrop instead of
              living in a small boxed slot — desktop only, matching
              SplineScene's own >=1024px / WebGL / reduced-motion gates, so
              nothing extra is fetched on mobile. */}
          <div className="absolute inset-0 hidden lg:block">
            <SplineScene />
          </div>

          {/* Scrim so the headline stays readable over the scene: solid on
              the text side, fading out before the robot so it isn't washed
              out entirely. Mobile has no scene, so no scrim either. */}
          <div
            aria-hidden
            className="absolute inset-0 hidden bg-gradient-to-r from-surface from-35% via-surface/75 via-55% to-transparent lg:block"
          />

          <div className="relative mx-auto flex w-full max-w-5xl flex-col px-5 pt-16 pb-10 sm:px-8 sm:pt-24 sm:pb-12 lg:min-h-[38rem] lg:justify-center lg:pr-28 lg:pb-16">
            <h1
              className="fade-rise-in max-w-3xl bg-gradient-to-br from-primary-900 via-primary-800 to-primary-600 bg-clip-text text-4xl font-semibold leading-[1.08] tracking-tight text-transparent sm:text-5xl"
            >
              Healthcare that keeps
              <br />
              its promises.
            </h1>
            <p
              style={{ "--fade-delay": 90 } as CSSProperties}
              className="fade-rise-in mt-5 max-w-xl text-base leading-relaxed text-text-secondary sm:text-lg"
            >
              Appointments, blood donors and health records — for patients,
              doctors and the people who run it all.
            </p>

            <div
              style={{ "--fade-delay": 160 } as CSSProperties}
              className="fade-rise-in mt-8 flex flex-wrap items-center gap-3"
            >
              <Link
                href="/login"
                className="group inline-flex h-12 items-center justify-center gap-2 rounded-lg bg-gradient-to-br from-primary-800 to-primary-900 px-7 text-sm font-medium text-white shadow-md shadow-primary-900/20 transition-all hover:shadow-lg hover:shadow-primary-900/25 active:scale-[0.98]"
              >
                Sign in
                <i aria-hidden className="bx bx-right-arrow-alt text-lg leading-none transition-transform group-hover:translate-x-0.5" />
              </Link>
              <Link
                href="/register"
                className="inline-flex h-12 items-center justify-center rounded-lg border border-border-strong bg-surface px-7 text-sm font-medium text-text-primary transition-all hover:border-primary-600 hover:bg-primary-50 active:scale-[0.98]"
              >
                Create an account
              </Link>
            </div>

            <div
              style={{ "--fade-delay": 220 } as CSSProperties}
              className="fade-rise-in mt-7 flex items-center gap-2 text-xs font-medium text-text-muted"
            >
              <i aria-hidden className="bx bxs-lock-alt text-sm leading-none text-primary-600" />
              Session-secured sign-in, HMAC-signed realtime tickets
            </div>
          </div>

          {/* Quick portal access, pinned to the very edge of the hero so it
              reads as a dock beside the scene rather than competing with the
              headline for the same column. The full write-up for each
              portal (blurb + secondary action) is still below — this is
              just the fast path. */}
          <div className="absolute inset-y-0 right-3 z-10 hidden flex-col items-center justify-center gap-3 sm:right-5 lg:flex">
            <HeroPortalButton href="/login?next=%2Fdashboard" icon="bxs-user" label="Patient" />
            <HeroPortalButton href="/doctor/login" icon="bxs-first-aid" label="Doctor" />
            <HeroPortalButton href="/admin/login" icon="bxs-shield-alt-2" label="Admin" />
          </div>
        </section>

        {/* ---- Portals ----
            The list is the page. One label per section, nothing else. */}
        <section
          aria-labelledby="portals-heading"
          className="relative mx-auto w-full max-w-5xl px-5 pb-16 sm:px-8 sm:pb-24"
        >
          <h2
            id="portals-heading"
            className="text-xs font-semibold tracking-[0.14em] text-text-muted uppercase"
          >
            Choose your portal
          </h2>

          <div className="glass-panel mt-4 overflow-hidden rounded-2xl px-5 shadow-md sm:px-7">
            <Suspense fallback={<PortalSkeleton />}>
              <RolePortals />
            </Suspense>
          </div>
        </section>
      </main>

      {/* ---- Footer ---- */}
      <footer className="border-t border-border bg-surface">
        <div className="mx-auto w-full max-w-5xl px-5 py-6 text-sm text-text-muted sm:px-8">
          © CloudCare
        </div>
      </footer>
    </div>
  );
}