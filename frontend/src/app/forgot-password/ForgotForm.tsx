"use client";

import Link from "next/link";
import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { FormField, Input } from "@/components/ui/Form";
import { resetPassword, type ResetState } from "./actions";

/**
 * Direct password reset: email plus a new password. The demo build no longer
 * depends on an email provider or a six-digit OTP.
 */
export function ForgotForm() {
  const [state, formAction, pending] = useActionState<ResetState, FormData>(
    resetPassword,
    undefined,
  );

  if (state?.done) {
    return (
      <div className="flex flex-col gap-4">
        <SuccessMessage>{state.message}</SuccessMessage>
        <p className="text-sm text-text-muted">
          You can now sign in with your new password.
        </p>
        <Link href="/login" className="block">
          <Button fullWidth>Back to sign in</Button>
        </Link>
      </div>
    );
  }

  return (
    <form action={formAction} className="flex flex-col gap-4">
      <FormField label="Email" htmlFor="email" required>
        <Input
          id="email"
          name="email"
          type="email"
          autoComplete="email"
          required
          placeholder="you@example.com"
        />
      </FormField>

      <FormField label="New password" htmlFor="password" required>
        <Input
          id="password"
          name="password"
          type="password"
          autoComplete="new-password"
          required
          minLength={6}
        />
      </FormField>

      <FormField label="Confirm new password" htmlFor="confirmPassword" required>
        <Input
          id="confirmPassword"
          name="confirmPassword"
          type="password"
          autoComplete="new-password"
          required
          minLength={6}
        />
      </FormField>

      {state?.error ? <ErrorMessage>{state.error}</ErrorMessage> : null}

      <Button type="submit" fullWidth disabled={pending}>
        {pending ? "Resetting…" : "Reset password"}
      </Button>

      <p className="text-center text-sm text-text-muted">
        <Link href="/login" className="hover:text-primary-600">
          Back to sign in
        </Link>
      </p>
    </form>
  );
}
