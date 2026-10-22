import Link from "next/link";
import { Suspense } from "react";

import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { getCurrentUser, HOME_BY_ROLE } from "@/lib/auth";

const FEATURES = [
  {
    icon: "❥",
    title: "Blood donation",
    description:
      "Post a request or register as a donor. We match blood groups and districts automatically.",
  },
  {
    icon: "⚕",
    title: "Doctor appointments",
    description:
      "Browse verified doctors, see live availability and book a slot in a few clicks.",
  },
  {
    icon: "❤",
    title: "Health records",
    description:
      "Keep vitals, lab results and lifestyle information in one secure place.",
  },
  {
    icon: "✦",
    title: "CloudCare AI",
    description:
      "Ask health questions any time and get clear, considered answers.",
  },
];

/** Header auth controls. Session read, so it streams in behind <Suspense>. */
async function AuthActions() {
  const user = await getCurrentUser();

  if (user) {
    return (
      <Link href={HOME_BY_ROLE[user.role]}>
        <Button size="sm">Go to dashboard</Button>
      </Link>
    );
  }
  return (
    <>
      <Link href="/login">
        <Button variant="ghost" size="sm">
          Sign in
        </Button>
      </Link>
      <Link href="/register">
        <Button size="sm">Get started</Button>
      </Link>
    </>
  );
}

function AuthActionsSkeleton() {
  return (
    <div className="flex items-center gap-2">
      <div className="h-8 w-16 animate-pulse rounded-md bg-neutral-200" />
      <div className="h-8 w-24 animate-pulse rounded-md bg-neutral-200" />
    </div>
  );
}

async function PrimaryCta() {
  const user = await getCurrentUser();
  return (
    <Link href={user ? HOME_BY_ROLE[user.role] : "/register"}>
      <Button size="lg">
        {user ? "Open your dashboard" : "Create an account"}
      </Button>
    </Link>
  );
}

async function SecondaryCta() {
  const user = await getCurrentUser();
  if (user) return null;
  return (
    <Link href="/login">
      <Button variant="secondary" size="lg">
        Sign in
      </Button>
    </Link>
  );
}

export default function LandingPage() {
  return (
    <div className="flex min-h-screen flex-col bg-surface">
      {/* ---- Header ---- */}
      <header className="border-b border-border">
        <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-6">
          <div className="flex items-center gap-2">
            <span className="flex size-7 items-center justify-center rounded-md bg-primary-600 text-sm font-bold text-white">
              C
            </span>
            <span className="font-semibold tracking-tight text-text-primary">
              CloudCare
            </span>
          </div>

          {/* Reads the session, so it streams in behind the boundary. */}
          <Suspense fallback={<AuthActionsSkeleton />}>
            <AuthActions />
          </Suspense>
        </div>
      </header>

      {/* ---- Hero ---- */}
      <main className="flex-1">
        <div className="mx-auto flex max-w-6xl flex-col gap-14 px-6 py-20">
          <div className="flex max-w-2xl flex-col gap-4">
            <h1 className="text-4xl font-semibold leading-tight tracking-tight text-text-primary">
              Healthcare that keeps you{" "}
              <span className="text-primary-600">connected</span>.
            </h1>
            <p className="text-lg text-text-secondary">
              Blood donation, doctor appointments and your health records — in
              one calm, simple place.
            </p>
            <div className="mt-2 flex items-center gap-3">
              <Suspense fallback={<Button size="lg">Create an account</Button>}>
                <PrimaryCta />
              </Suspense>
              <Suspense fallback={null}>
                <SecondaryCta />
              </Suspense>
            </div>
          </div>

          {/* ---- Features ---- */}
          <div className="grid grid-cols-2 gap-4">
            {FEATURES.map((feature) => (
              <Card key={feature.title} className="p-6">
                <span className="flex size-9 items-center justify-center rounded-md bg-primary-50 text-lg text-primary-600">
                  <span aria-hidden>{feature.icon}</span>
                </span>
                <h2 className="mt-4 font-semibold text-text-primary">
                  {feature.title}
                </h2>
                <p className="mt-1 text-sm text-text-secondary">
                  {feature.description}
                </p>
              </Card>
            ))}
          </div>
        </div>
      </main>

      {/* ---- Footer ---- */}
      <footer className="border-t border-border">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-6">
          <p className="text-sm text-text-muted">
            © CloudCare. Built for safer, simpler care.
          </p>
        </div>
      </footer>
    </div>
  );
}