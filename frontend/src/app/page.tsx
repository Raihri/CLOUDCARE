import Link from "next/link";
import { Suspense } from "react";
import type { CSSProperties } from "react";

import { BrandLogo } from "@/components/brand/BrandLogo";
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

  return null;
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
    <div className="flex min-h-screen flex-col bg-surface">
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

          {/* The scene fills the whole hero as a backdrop. A pure CSS
              transform (not a resize — resizing the canvas changes Spline's
              own camera FOV and reveals MORE content instead of cropping)
              zooms into the already-rendered frame and, anchored at the
              left edge, pushes everything further right as it scales up —
              away from the text column instead of growing toward it. */}
          <div className="absolute inset-0 hidden origin-left scale-150 lg:block">
            <SplineScene />
          </div>

          {/* Scrim so the headline stays readable over the scene: solid
              right under the text, gone well before the scene's own content
              so the scene isn't washed out. */}
          <div
            aria-hidden
            className="absolute inset-0 hidden bg-gradient-to-r from-surface from-0% via-surface/35 via-18% to-transparent to-32% lg:block"
          />

          <div className="relative mx-auto flex w-full max-w-5xl flex-col px-5 pt-16 pb-10 sm:px-8 sm:pt-24 sm:pb-12 lg:min-h-[38rem] lg:justify-center lg:pb-16">
            <div className="lg:max-w-sm">
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
            </div>

            {/* Mobile/tablet equivalent of the edge-docked rail below: the
                scene and the absolute dock are both lg:-only (no WebGL
                asset fetched, no overlap risk), so without this a phone
                visitor would have no portal entry point at all. Row instead
                of column — there's no vertical edge to dock against here. */}
            <div
              style={{ "--fade-delay": 160 } as CSSProperties}
              className="fade-rise-in mt-8 lg:hidden"
            >
              <p className="mb-2 text-[11px] font-semibold tracking-[0.14em] text-text-muted uppercase">
                Sign in as
              </p>
              <div className="flex items-center gap-3">
                <HeroPortalButton href="/login?next=%2Fdashboard" icon="bxs-user" label="Patient" />
                <HeroPortalButton href="/doctor/login" icon="bxs-first-aid" label="Doctor" />
                <HeroPortalButton href="/admin/login" icon="bxs-shield-alt-2" label="Admin" />
              </div>
            </div>
          </div>

          {/* Quick portal access, pinned to the very edge of the hero so it
              reads as a dock beside the scene rather than competing with the
              headline for the same column. */}
          <div className="absolute inset-y-0 right-3 z-10 hidden flex-col items-center justify-center gap-3 sm:right-5 lg:flex">
            <p className="mb-1 text-[10px] font-semibold tracking-[0.14em] text-text-muted uppercase">
              Sign in
            </p>
            <HeroPortalButton href="/login?next=%2Fdashboard" icon="bxs-user" label="Patient" />
            <HeroPortalButton href="/doctor/login" icon="bxs-first-aid" label="Doctor" />
            <HeroPortalButton href="/admin/login" icon="bxs-shield-alt-2" label="Admin" />
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