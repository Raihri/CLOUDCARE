import type { Metadata } from "next";
import Link from "next/link";
import { Suspense } from "react";

import { AuthCard } from "@/components/auth/AuthCard";
import { login } from "./actions";
import { LoginForm } from "./LoginForm";

export const metadata: Metadata = { title: "Sign in" };

export default function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ next?: string }>;
}) {
  return (
    <AuthCard
      icon="C"
      title="Sign in to CloudCare"
      subtitle="Donate blood, book appointments, track your health."
      footer={
        <>
          <p className="mt-4 text-center text-sm text-text-muted">
            <Link href="/forgot-password" className="hover:text-primary-600">
              Forgot your password?
            </Link>
          </p>

          <p className="mt-4 text-center text-sm text-text-muted">
            New to CloudCare?{" "}
            <Link
              href="/register"
              className="font-medium text-primary-600 hover:text-primary-700"
            >
              Create an account
            </Link>
          </p>
        </>
      }
    >
      {/* Reads ?next= so the post-login redirect can return the user to
          wherever a guard interrupted them. Must sit behind a Suspense
          boundary for this route to stay prerenderable. */}
      <Suspense fallback={<LoginFormFallback />}>
        <LoginFormWithNext searchParams={searchParams} />
      </Suspense>
    </AuthCard>
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