"use client";

import Link from "next/link";
import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { FormField, Input } from "@/components/ui/Form";
import {
  requestPasswordReset,
  setNewPassword,
  type ResetRole,
  type ResetState,
} from "@/lib/password-reset";

const LOGIN_PATH: Record<ResetRole, string> = {
  patient: "/login",
  doctor: "/doctor/login",
};

const FORGOT_PATH: Record<ResetRole, string> = {
  patient: "/forgot-password",
  doctor: "/doctor/forgot-password",
};

/** Step 1: ask for the account email and send a reset link. */
export function RequestResetForm({ role }: { role: ResetRole }) {
  const [state, formAction, pending] = useActionState<ResetState, FormData>(
    requestPasswordReset.bind(null, role),
    undefined,
  );

  if (state?.done) {
    return (
      <div className="flex flex-col gap-4">
        <SuccessMessage>{state.message}</SuccessMessage>
        <p className="text-sm text-text-muted">
          Open the link in the email to choose a new password. Check your spam
          folder if it doesn&apos;t arrive within a few minutes.
        </p>
        <Link href={LOGIN_PATH[role]} className="block">
          <Button fullWidth variant="secondary">
            Back to sign in
          </Button>
        </Link>
      </div>
    );
  }

  return (
    <form action={formAction} className="flex flex-col gap-4">
      <FormField
        label="Email"
        htmlFor="email"
        required
        hint="We'll email you a link to choose a new password."
      >
        <Input
          id="email"
          name="email"
          type="email"
          autoComplete="email"
          required
          placeholder="you@example.com"
        />
      </FormField>

      {state?.error ? <ErrorMessage>{state.error}</ErrorMessage> : null}

      <Button type="submit" fullWidth disabled={pending}>
        {pending ? "Sending…" : "Send reset link"}
      </Button>

      <p className="text-center text-sm text-text-muted">
        <Link href={LOGIN_PATH[role]} className="hover:text-primary-600">
          Back to sign in
        </Link>
      </p>
    </form>
  );
}

/** Step 2: the page the emailed link opens. Sets the new password. */
export function ChooseNewPasswordForm({
  role,
  token,
}: {
  role: ResetRole;
  token: string;
}) {
  const [state, formAction, pending] = useActionState<ResetState, FormData>(
    setNewPassword.bind(null, role),
    undefined,
  );

  if (!token) {
    return (
      <div className="flex flex-col gap-4">
        <ErrorMessage>This reset link is incomplete. Please request a new one.</ErrorMessage>
        <Link href={FORGOT_PATH[role]} className="block">
          <Button fullWidth>Request a new link</Button>
        </Link>
      </div>
    );
  }

  if (state?.done) {
    return (
      <div className="flex flex-col gap-4">
        <SuccessMessage>{state.message}</SuccessMessage>
        <Link href={LOGIN_PATH[role]} className="block">
          <Button fullWidth>Sign in</Button>
        </Link>
      </div>
    );
  }

  return (
    <form action={formAction} className="flex flex-col gap-4">
      <input type="hidden" name="token" value={token} />

      <FormField label="New password" htmlFor="password" required hint="At least 8 characters.">
        <Input
          id="password"
          name="password"
          type="password"
          autoComplete="new-password"
          required
          minLength={8}
        />
      </FormField>

      <FormField label="Confirm new password" htmlFor="confirmPassword" required>
        <Input
          id="confirmPassword"
          name="confirmPassword"
          type="password"
          autoComplete="new-password"
          required
          minLength={8}
        />
      </FormField>

      {state?.error ? (
        <ErrorMessage>
          {state.error}{" "}
          <Link href={FORGOT_PATH[role]} className="underline">
            Request a new link
          </Link>
        </ErrorMessage>
      ) : null}

      <Button type="submit" fullWidth disabled={pending}>
        {pending ? "Saving…" : "Set new password"}
      </Button>
    </form>
  );
}
