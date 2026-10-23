"use client";

import { useActionState } from "react";

import { login, type LoginState } from "@/app/login/actions";
import { DEMO_ACCOUNTS, type DemoAccount } from "@/lib/demo-accounts";
import type { Role } from "@/lib/auth";

/**
 * One-click sign-in for the seeded demo roles.
 *
 * Each account gets its own form so the pending/error state of one button does
 * not affect the other two. The credentials live in `lib/demo-accounts.ts` and
 * are also printed on the page for reviewers who want to use the normal form.
 *
 * On a role-specific login page, pass `role` to keep the shortcut scoped to
 * that portal. The generic /login page omits it and shows all three.
 */
export function QuickLogin({
  compact = false,
  role,
}: {
  compact?: boolean;
  role?: Role;
}) {
  const accounts = role
    ? DEMO_ACCOUNTS.filter((account) => account.role === role)
    : DEMO_ACCOUNTS;

  if (accounts.length === 0) return null;

  const single = accounts.length === 1;

  return (
    <section
      aria-labelledby="demo-login-heading"
      className={compact ? "mt-5" : "mt-6"}
    >
      <div className="flex items-center gap-3">
        <span className="h-px flex-1 bg-border" />
        {/* Deliberately loud. Reviewers open the login page expecting a real
            credential box and this is the one thing on screen that is not one —
            at 10px muted it read as a footnote. Now 12px, bold, primary-tinted
            with a soft chip behind it. */}
        <h2
          id="demo-login-heading"
          className="rounded-md bg-primary-50 px-2.5 py-1 text-xs font-bold uppercase tracking-[0.12em] text-primary-700"
        >
          Demo access
        </h2>
        <span className="h-px flex-1 bg-border" />
      </div>

      <p className="mt-2.5 text-center text-[11px] leading-relaxed text-text-muted">
        {single
          ? "One-click access to the seeded demo account."
          : "One-click access to a seeded demo account."}
      </p>

      <div
        className={
          compact && !single
            ? "mt-2.5 grid grid-cols-3 gap-2"
            : "mt-2.5 grid min-w-0 gap-2"
        }
      >
        {accounts.map((account) => (
          <QuickLoginButton
            key={account.role}
            account={account}
            dense={compact && !single}
          />
        ))}
      </div>
    </section>
  );
}

function QuickLoginButton({
  account,
  dense = false,
}: {
  account: DemoAccount;
  dense?: boolean;
}) {
  const [state, formAction, pending] = useActionState<LoginState, FormData>(
    login,
    undefined,
  );

  if (dense) {
    return (
      <form action={formAction} className="min-w-0">
        <input type="hidden" name="email" value={account.email} />
        <input type="hidden" name="password" value={account.password} />
        <input type="hidden" name="role" value={account.role} />

        <button
          type="submit"
          disabled={pending}
          title={`${account.name} — ${account.email}`}
          className="group flex w-full flex-col items-center gap-1 rounded-lg border border-border bg-surface px-2 py-2 transition-colors hover:border-primary-300 hover:bg-primary-50 disabled:cursor-wait disabled:opacity-60"
        >
          <span className="flex size-7 items-center justify-center rounded-md bg-primary-50 text-primary-600 group-hover:bg-primary-100">
            <i aria-hidden className={`bx ${account.icon} text-base leading-none`} />
          </span>
          <span className="text-[10px] font-medium text-text-primary">
            {pending ? "Opening…" : account.label}
          </span>
        </button>

        {state?.error ? (
          <p role="alert" className="mt-1 text-[10px] leading-snug text-danger">
            {state.error}
          </p>
        ) : null}
      </form>
    );
  }

  return (
    <form action={formAction} className="min-w-0">
      <input type="hidden" name="email" value={account.email} />
      <input type="hidden" name="password" value={account.password} />
      <input type="hidden" name="role" value={account.role} />

      <button
        type="submit"
        disabled={pending}
        className="group flex w-full items-center gap-2.5 rounded-lg border border-border bg-surface px-3 py-2 text-left transition-colors hover:border-primary-300 hover:bg-primary-50 disabled:cursor-wait disabled:opacity-60"
      >
        <span className="flex size-7 shrink-0 items-center justify-center rounded-md bg-primary-50 text-primary-600 group-hover:bg-primary-100">
          <i aria-hidden className={`bx ${account.icon} text-base leading-none`} />
        </span>
        <span className="min-w-0 flex-1">
          <span className="flex items-baseline gap-1.5">
            <span className="text-xs font-medium text-text-primary">
              {account.label}
            </span>
            <span className="truncate text-[10px] text-text-muted">
              {account.name}
            </span>
          </span>
        </span>
        <span className="shrink-0 text-[11px] font-medium text-primary-600">
          {pending ? "Opening…" : "Open →"}
        </span>
      </button>

      {state?.error ? (
        <p role="alert" className="mt-1 px-1 text-xs text-danger">
          {state.error}
        </p>
      ) : null}
    </form>
  );
}
