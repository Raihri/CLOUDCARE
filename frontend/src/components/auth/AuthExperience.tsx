"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";

import { login } from "@/app/login/actions";
import { LoginForm } from "@/app/login/LoginForm";
import { RegisterForm } from "@/app/register/RegisterForm";
import { BrandLogo } from "@/components/brand/BrandLogo";
import { SuccessMessage } from "@/components/ui/Feedback";
import type { Role } from "@/lib/auth";
import { QuickLogin } from "./QuickLogin";
import { RoleFigure } from "./RoleFigure";

type AuthMode = "login" | "signup";

const ROLE_ORDER: Role[] = ["PATIENT", "DOCTOR", "ADMIN"];

/**
 * Deliberately short. The earlier version of this screen carried a wordmark,
 * a role pill, an eyebrow, a headline, a paragraph and three feature chips per
 * role — seven pieces of marketing copy stacked above a form that only has two
 * fields. The user asked for "as simple and minimal as possible", so the
 * marketing layer is gone entirely: a role label, one line of plain copy, and
 * the illustration. `poster` / `badge` / `dot` / `accentText` remain because
 * they are theme tokens, not words.
 */
type RoleMeta = {
  shortLabel: string;
  /** One plain sentence, shown under the sign-in heading. No marketing. */
  tagline: string;
  icon: string;
  poster: string;
  badge: string;
  dot: string;
  accentText: string;
  submitLabel: string;
  loginTitle: string;
  watermarkIcon: string;
};

const ROLE_META: Record<Role, RoleMeta> = {
  PATIENT: {
    shortLabel: "Patient",
    tagline: "Sign in to your account.",
    icon: "bxs-user",
    poster: "bg-linear-to-br from-primary-900 via-primary-800 to-primary-700",
    badge: "border-mint/25 bg-mint/10 text-mint",
    dot: "bg-mint",
    accentText: "text-mint",
    submitLabel: "Sign in",
    loginTitle: "Patient sign in",
    watermarkIcon: "bxs-heart",
  },
  DOCTOR: {
    shortLabel: "Doctor",
    tagline: "Sign in to your account.",
    icon: "bxs-first-aid",
    poster: "bg-linear-to-br from-primary-900 via-primary-800 to-primary-600",
    badge: "border-highlight/25 bg-highlight/10 text-highlight",
    dot: "bg-highlight",
    accentText: "text-highlight",
    submitLabel: "Sign in",
    loginTitle: "Doctor sign in",
    watermarkIcon: "bxs-first-aid",
  },
  ADMIN: {
    shortLabel: "Admin",
    tagline: "Sign in to your account.",
    icon: "bxs-shield-alt-2",
    poster: "bg-linear-to-br from-sidebar via-primary-900 to-primary-800",
    badge: "border-primary-300/30 bg-primary-300/10 text-primary-200",
    dot: "bg-primary-300",
    accentText: "text-primary-200",
    submitLabel: "Sign in",
    loginTitle: "Admin sign in",
    watermarkIcon: "bxs-shield-alt-2",
  },
};

export type AuthExperienceProps = {
  /** Which panel is visible on first paint. */
  initialMode?: AuthMode;
  /**
   * Locks the page to one portal. Leave it out on the generic /login route so
   * the patient / doctor / admin picker can drive both the form and the art.
   */
  role?: Role;
  /**
   * Preselects a role on the generic picker without locking the page. The
   * login page derives this from a guarded `?next=` path so a doctor bounced
   * out of /doctor/patients lands on the doctor figure rather than the patient
   * one.
   */
  initialRole?: Role;
  next?: string;
  initialEmail?: string;
  registered?: boolean;
  forgotHref?: string;
  /**
   * A full-page destination for portals that cannot register inline, e.g.
   * /doctor/signup. Admin has no self-service route at all.
   */
  signupHref?: string;
  /** Enables the patient registration panel and the cross-switch controls. */
  showSignup?: boolean;
};

/**
 * The signed-out experience shared by /login, /register and both portal login
 * routes.
 *
 * It keeps the supplied template's best idea — a form on one side and an
 * overlay that slides across when you switch between sign-in and sign-up — but
 * replaces the decorative waves with the brand palette, a clinical heartbeat
 * line and a role-specific portrait. On phones the overlay collapses into a
 * compact banner above the form so nothing is hidden behind a hover-only
 * interaction.
 */
export function AuthExperience({
  initialMode = "login",
  role,
  initialRole,
  next,
  initialEmail,
  registered = false,
  forgotHref,
  signupHref,
  showSignup = false,
}: AuthExperienceProps) {
  const [mode, setMode] = useState<AuthMode>(
    showSignup ? initialMode : "login",
  );
  const [selectedRole, setSelectedRole] = useState<Role>(
    role ?? initialRole ?? "PATIENT",
  );

  const isSignup = mode === "signup";
  const activeRole: Role = isSignup ? "PATIENT" : (role ?? selectedRole);
  const meta = ROLE_META[activeRole];

  const hasInteracted = useRef(false);
  const loginHeading = useRef<HTMLHeadingElement>(null);
  const signupHeading = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    if (!hasInteracted.current) return;
    const target = isSignup ? signupHeading.current : loginHeading.current;
    target?.focus();
  }, [isSignup]);

  function changeMode(nextMode: AuthMode) {
    if (!showSignup) return;
    hasInteracted.current = true;
    if (nextMode === "signup") setSelectedRole("PATIENT");
    setMode(nextMode);
  }

  const loginCopy = {
    title: meta.loginTitle,
    subtitle: meta.tagline,
  };

  const signupCopy = {
    title: "Create your account",
    subtitle: "A free patient account.",
  };

  return (
    <main className="relative min-h-screen overflow-hidden bg-surface-sunken">
      <div className="relative mx-auto flex min-h-screen w-full max-w-7xl px-3 py-5 sm:px-6 sm:py-8 lg:px-8 lg:py-4">
        <div className="relative my-auto w-full overflow-hidden rounded-[1.75rem] border border-border bg-surface shadow-lg lg:h-[calc(100dvh-2rem)] lg:max-h-[50rem] lg:min-h-[38rem] lg:rounded-[2rem]">
          <MobileHero role={activeRole} meta={meta} />

          {/* The sliding overlay. In sign-in it parks on the right; switching
              to sign-up slides it over to the left while the form slides into
              the vacated half. */}
          <div
            className={[
              "absolute inset-y-0 left-0 z-20 hidden w-1/2 transition-transform duration-700 ease-[cubic-bezier(.22,1,.36,1)] motion-reduce:transition-none lg:block",
              isSignup ? "translate-x-0" : "translate-x-full",
            ].join(" ")}
          >
            <PosterPanel
              role={activeRole}
              meta={meta}
              mode={mode}
              showSignup={showSignup}
              signupHref={signupHref}
              onChangeMode={changeMode}
            />
          </div>

          <div className="relative z-10 lg:absolute lg:inset-y-0 lg:left-0 lg:w-1/2">
            {/* ---- Sign in ---- */}
            <section
              aria-hidden={isSignup || undefined}
              inert={isSignup}
              className={[
                // `lg:w-full` is load-bearing. An absolutely-positioned box shrink-wraps to its
                // content by default, so without it this section measured 330px
                // inside a 607px half and left a ~317px empty band between the
                // form and the poster. Stretching it to the half's width lets the
                // inner `max-w-[34rem]` column centre properly.
                "min-h-full px-5 py-7 transition-all duration-500 motion-reduce:transition-none sm:px-8 sm:py-10 lg:absolute lg:inset-y-0 lg:left-0 lg:w-full lg:flex lg:items-center lg:px-10 lg:py-10",
                isSignup
                  ? "hidden lg:-translate-y-4 lg:pointer-events-none lg:opacity-0"
                  : "flex",
              ].join(" ")}
            >
              {/* Centres within the (now full-width) half. The cap keeps the inputs at a
                    comfortable reading width on very wide screens while letting
                    them grow past the old fixed 29rem on narrower ones, so there
                    is no dead band between the form and the poster. */}
                <div className="relative mx-auto w-full max-w-[26rem]">
                <RoleWatermark meta={meta} />

                <div className="relative">
                  <div className="mb-5 flex justify-end lg:mb-7">
                    <Link
                      href="/"
                      className="inline-flex items-center gap-1 text-xs font-medium text-text-muted transition-colors hover:text-primary-600"
                    >
                      <i aria-hidden className="bx bx-left-arrow-alt text-base leading-none" />
                      Back to CloudCare
                    </Link>
                  </div>

                  {showSignup ? (
                    <MobileModeSwitch
                      mode={mode}
                      onChangeMode={changeMode}
                    />
                  ) : null}

                  <div className="mb-5">
                    <h1
                      ref={loginHeading}
                      tabIndex={-1}
                      className="text-[1.65rem] font-semibold leading-tight tracking-tight text-text-primary outline-none focus-visible:outline-none sm:text-[1.8rem]"
                    >
                      {loginCopy.title}
                    </h1>
                    <p className="mt-1.5 text-sm leading-relaxed text-text-secondary">
                      {loginCopy.subtitle}
                    </p>
                  </div>

                  {registered ? (
                    <div className="mb-4">
                      <SuccessMessage>
                        Account created. Sign in below — or use a demo account.
                      </SuccessMessage>
                    </div>
                  ) : null}

                  {!role && !isSignup ? (
                    <RolePicker
                      activeRole={activeRole}
                      onChange={(nextRole) => setSelectedRole(nextRole)}
                    />
                  ) : null}

                  <LoginForm
                    action={login}
                    next={next}
                    role={role}
                    selectedRole={role ? undefined : activeRole}
                    initialEmail={initialEmail}
                    forgotHref={forgotHref}
                    idPrefix="login"
                    submitLabel={meta.submitLabel}
                  />

                  <QuickLogin compact role={role} />

                  {role === "DOCTOR" && signupHref ? (
                    <p className="mt-4 text-center text-xs text-text-muted">
                      New clinician?{" "}
                      <Link
                        href={signupHref}
                        className="font-medium text-primary-600 transition-colors hover:text-primary-700"
                      >
                        Apply as a doctor
                      </Link>
                    </p>
                  ) : null}

                  {role === "ADMIN" ? (
                    <p className="mt-4 rounded-lg border border-border bg-neutral-50 px-3 py-2 text-center text-xs leading-relaxed text-text-muted lg:hidden">
                      Admin accounts are provisioned by CloudCare operations.
                    </p>
                  ) : null}

                  {showSignup ? (
                    <p className="mt-5 text-center text-sm text-text-muted lg:hidden">
                      New to CloudCare?{" "}
                      <button
                        type="button"
                        onClick={() => changeMode("signup")}
                        className="font-medium text-primary-600 transition-colors hover:text-primary-700"
                      >
                        Create an account
                      </button>
                    </p>
                  ) : null}
                </div>
              </div>
            </section>

            {/* ---- Sign up ---- */}
            {showSignup ? (
              <section
                aria-hidden={!isSignup || undefined}
                inert={!isSignup}
                className={[
                  // Same `lg:w-full` requirement as the sign-in section above.
                  "min-h-full px-5 py-7 transition-all duration-700 motion-reduce:transition-none sm:px-8 sm:py-10 lg:absolute lg:inset-y-0 lg:left-0 lg:w-full lg:flex lg:items-center lg:px-10 lg:py-10",
                  isSignup
                    ? "flex lg:translate-x-full lg:opacity-100"
                    : "hidden lg:translate-x-0 lg:pointer-events-none lg:opacity-0",
                ].join(" ")}
              >
                <div className="relative mx-auto w-full max-w-[29rem]">
                  <RoleWatermark meta={ROLE_META.PATIENT} />

                  <div className="relative">
                    <div className="mb-5 flex justify-end lg:mb-7">
                      <Link
                        href="/"
                        className="inline-flex items-center gap-1 text-xs font-medium text-text-muted transition-colors hover:text-primary-600"
                      >
                        <i aria-hidden className="bx bx-left-arrow-alt text-base leading-none" />
                        Back to CloudCare
                      </Link>
                    </div>

                    <MobileModeSwitch mode={mode} onChangeMode={changeMode} />

                    <div className="mb-5">
                      <h1
                        ref={signupHeading}
                        tabIndex={-1}
                        className="text-[1.65rem] font-semibold leading-tight tracking-tight text-text-primary outline-none focus-visible:outline-none sm:text-[1.8rem]"
                      >
                        {signupCopy.title}
                      </h1>
                      <p className="mt-1.5 text-sm leading-relaxed text-text-secondary">
                        {signupCopy.subtitle}
                      </p>
                    </div>

                    <RegisterForm idPrefix="signup" />

                    <p className="mt-5 text-center text-sm text-text-muted">
                      Already have an account?{" "}
                      <button
                        type="button"
                        onClick={() => changeMode("login")}
                        className="font-medium text-primary-600 transition-colors hover:text-primary-700"
                      >
                        Sign in
                      </button>
                    </p>
                    <p className="mt-2 text-center text-xs leading-relaxed text-text-muted">
                      Patient and donor accounts. Clinicians can{" "}
                      <Link
                        href="/doctor/signup"
                        className="font-medium text-text-secondary underline-offset-2 hover:text-primary-600 hover:underline"
                      >
                        apply here
                      </Link>
                      .
                    </p>
                  </div>
                </div>
              </section>
            ) : null}
          </div>

          <p className="sr-only" aria-live="polite">
            {isSignup ? "Sign-up form shown" : "Sign-in form shown"}
          </p>
        </div>
      </div>
    </main>
  );
}

function MobileHero({
  role,
  meta,
}: {
  role: Role;
  meta: RoleMeta;
}) {
  return (
    <section
      className={[
        "relative overflow-hidden px-5 pb-5 pt-5 text-text-inverted sm:px-7 lg:hidden",
        meta.poster,
      ].join(" ")}
    >
      <div aria-hidden className="absolute inset-0">
        <div className={`absolute -top-20 -right-16 size-64 rounded-full ${meta.accentText} opacity-15 blur-3xl`} />
        <div className="absolute -bottom-24 -left-20 size-56 rounded-full bg-primary-600/25 blur-3xl" />
        <PulseLine
          className={`absolute inset-x-0 bottom-0 h-12 opacity-20 ${meta.accentText}`}
        />
      </div>

      <div className="relative flex items-center gap-3">
        <Link href="/" aria-label="CloudCare home" className="shrink-0">
          <BrandLogo size={30} priority className="ring-1 ring-white/20" />
        </Link>
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-semibold tracking-tight">
            {meta.shortLabel}
          </p>
        </div>
        {/* Small enough that the full 360x420 viewBox is inside the frame — the
            figures were clipping their floating props at the old width. */}
        <div key={role} className="auth-swap-in w-16 shrink-0 sm:w-20">
          <RoleFigure role={role} />
        </div>
      </div>
    </section>
  );
}

function PosterPanel({
  role,
  meta,
  mode,
  showSignup,
  signupHref,
  onChangeMode,
}: {
  role: Role;
  meta: RoleMeta;
  mode: AuthMode;
  showSignup: boolean;
  signupHref?: string;
  onChangeMode: (mode: AuthMode) => void;
}) {
  return (
    <div
      className={[
        "relative flex h-full flex-col overflow-hidden px-8 py-8 text-text-inverted xl:px-10 xl:py-9",
        meta.poster,
      ].join(" ")}
    >
      <div aria-hidden className="absolute inset-0">
        <div className={`absolute -top-20 -right-16 size-72 rounded-full ${meta.accentText} opacity-20 blur-3xl`} />
        <div className="absolute -bottom-28 -left-20 size-72 rounded-full bg-primary-600/25 blur-3xl" />
        <PulseLine
          className={`absolute inset-x-0 bottom-24 h-16 opacity-20 ${meta.accentText}`}
        />
      </div>

      <div className="relative flex items-center justify-between gap-3">
        <Link href="/" aria-label="CloudCare home">
          <BrandLogo size={32} priority className="ring-1 ring-white/20" />
        </Link>
      </div>

      <div className="relative flex flex-1 flex-col items-center justify-center">
        {/* Half the old width. The viewBox is 360x420 and the figures draw
            props out to x=320, so at 17.5rem the panel's own padding started
            clipping them; 9rem keeps the whole 360x420 box on screen. */}
        <div key={role} className="auth-swap-in w-full max-w-[9rem]">
          <RoleFigure role={role} />
        </div>

        <div key={`${role}-copy`} className="auth-swap-in mt-5 text-center">
          <h2 className="text-xl font-semibold leading-tight tracking-tight text-white">
            {meta.shortLabel} portal
          </h2>
          <p className="mt-1.5 text-sm text-white/55">{meta.tagline}</p>
        </div>
      </div>

      <div className="relative mt-5">
        {mode === "signup" ? (
          <button
            type="button"
            onClick={() => onChangeMode("login")}
            className="group flex w-full items-center justify-center gap-1.5 rounded-xl border border-white/20 bg-white/5 px-4 py-3 text-sm text-white/70 transition-colors hover:border-white/35 hover:bg-white/10 hover:text-white"
          >
            Already have an account?
            <span className="font-semibold text-white">Sign in</span>
            <i aria-hidden className="bx bx-right-arrow-alt text-lg leading-none transition-transform group-hover:translate-x-0.5" />
          </button>
        ) : showSignup ? (
          <button
            type="button"
            onClick={() => onChangeMode("signup")}
            className="group flex w-full items-center justify-center gap-1.5 rounded-xl border border-white/20 bg-white/5 px-4 py-3 text-sm text-white/70 transition-colors hover:border-white/35 hover:bg-white/10 hover:text-white"
          >
            New to CloudCare?
            <span className="font-semibold text-white">Create an account</span>
            <i aria-hidden className="bx bx-right-arrow-alt text-lg leading-none transition-transform group-hover:translate-x-0.5" />
          </button>
        ) : signupHref ? (
          <Link
            href={signupHref}
            className="group flex w-full items-center justify-center gap-1.5 rounded-xl border border-white/20 bg-white/5 px-4 py-3 text-sm text-white/70 transition-colors hover:border-white/35 hover:bg-white/10 hover:text-white"
          >
            Want to join CloudCare?
            <span className="font-semibold text-white">Apply as a doctor</span>
            <i aria-hidden className="bx bx-right-arrow-alt text-lg leading-none transition-transform group-hover:translate-x-0.5" />
          </Link>
        ) : (
          <p className="rounded-xl border border-white/10 bg-white/5 px-4 py-3 text-center text-xs leading-relaxed text-white/55">
            Admin accounts are provisioned by CloudCare operations.
          </p>
        )}

        <p className="mt-3 flex items-center justify-center gap-1.5 text-[10px] text-white/35">
          <i aria-hidden className="bx bx-lock-alt text-sm leading-none" />
          Secure, role-based access
        </p>
      </div>
    </div>
  );
}

function RolePicker({
  activeRole,
  onChange,
}: {
  activeRole: Role;
  onChange: (role: Role) => void;
}) {
  return (
    <div className="mb-4">
      <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-text-muted">
        Choose your portal
      </p>
      <div
        role="group"
        aria-label="Sign in as"
        className="mt-2 grid grid-cols-3 gap-1 rounded-xl bg-neutral-100 p-1"
      >
        {ROLE_ORDER.map((candidate) => {
          const candidateMeta = ROLE_META[candidate];
          const selected = activeRole === candidate;

          return (
            <button
              key={candidate}
              type="button"
              aria-pressed={selected}
              onClick={() => onChange(candidate)}
              className={[
                "flex items-center justify-center gap-1.5 rounded-lg px-1.5 py-2 text-[11px] font-medium transition-all",
                selected
                  ? "bg-surface text-primary-900 shadow-sm"
                  : "text-text-muted hover:bg-surface/60 hover:text-text-primary",
              ].join(" ")}
            >
              <i
                aria-hidden
                className={`bx ${candidateMeta.icon} text-sm leading-none ${
                  selected ? "text-primary-600" : ""
                }`}
              />
              {candidateMeta.shortLabel}
            </button>
          );
        })}
      </div>
    </div>
  );
}

function MobileModeSwitch({
  mode,
  onChangeMode,
}: {
  mode: AuthMode;
  onChangeMode: (mode: AuthMode) => void;
}) {
  return (
    <div
      role="group"
      aria-label="Account access"
      className="mb-5 grid grid-cols-2 gap-1 rounded-xl bg-neutral-100 p-1 lg:hidden"
    >
      <button
        type="button"
        aria-pressed={mode === "login"}
        onClick={() => onChangeMode("login")}
        className={[
          "rounded-lg px-2 py-2 text-xs font-medium transition-all",
          mode === "login"
            ? "bg-surface text-primary-900 shadow-sm"
            : "text-text-muted hover:text-text-primary",
        ].join(" ")}
      >
        Sign in
      </button>
      <button
        type="button"
        aria-pressed={mode === "signup"}
        onClick={() => onChangeMode("signup")}
        className={[
          "rounded-lg px-2 py-2 text-xs font-medium transition-all",
          mode === "signup"
            ? "bg-surface text-primary-900 shadow-sm"
            : "text-text-muted hover:text-text-primary",
        ].join(" ")}
      >
        Create account
      </button>
    </div>
  );
}

function RoleWatermark({ meta }: { meta: RoleMeta }) {
  return (
    <div
      aria-hidden
      className="pointer-events-none absolute -right-10 -bottom-10 hidden select-none text-[12rem] leading-none text-primary-900/[0.035] lg:block"
    >
      <i className={`bx ${meta.watermarkIcon}`} />
    </div>
  );
}

function PulseLine({ className = "" }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 720 64"
      preserveAspectRatio="none"
      aria-hidden="true"
      className={className}
    >
      <path
        d="M0 32H212L226 32L238 8L252 56L266 20L280 32H720"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        className="auth-pulse-line"
      />
    </svg>
  );
}
