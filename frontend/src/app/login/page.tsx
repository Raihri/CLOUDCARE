import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";

import { login } from "./actions";
import { LoginForm } from "./LoginForm";

export const metadata: Metadata = { title: "Sign in" };

export default function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ next?: string }>;
}) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-surface-sunken px-6 py-12">
      <div className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center text-center">
          <span className="flex size-11 items-center justify-center rounded-xl bg-primary-600 text-lg font-bold text-white">
            C
          </span>
          <h1 className="mt-4 text-xl font-semibold tracking-tight text-text-primary">
            Sign in to CloudCare
          </h1>
          <p className="mt-1 text-sm text-text-muted">
            Donate blood, book appointments, track your health.
          </p>
        </div>

        <div className="rounded-lg border border-border bg-surface p-6 shadow-sm">
          {/* Reads ?next= so the post-login redirect can return the user to
              wherever a guard interrupted them. Must sit behind a Suspense
              boundary for this route to stay prerenderable. */}
          <Suspense fallback={<LoginFormFallback />}>
            <LoginFormWithNext searchParams={searchParams} />
          </Suspense>
        </div>

        <p className="mt-4 text-center text-sm text-text-muted">
          New to CloudCare?{" "}
          <Link
            href="/register"
            className="font-medium text-primary-600 hover:text-primary-700"
          >
            Create an account
          </Link>
        </p>
      </div>
    </div>
  );
}

async function LoginFormWithNext({
  searchParams,
}: {
  searchParams: Promise<{ next?: string }>;
}) {
  const { next } = await searchParams;
  return <LoginForm action={login} next={next} />;
}

function LoginFormFallback() {
  return (
    <div className="flex flex-col gap-4" aria-busy="true">
      <div className="h-10 animate-pulse rounded-md bg-neutral-200" />
      <div className="h-10 animate-pulse rounded-md bg-neutral-200" />
      <div className="h-10 animate-pulse rounded-md bg-neutral-200" />
    </div>
  );
}