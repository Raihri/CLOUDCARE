"use client";

import { useActionState } from "react";
import Link from "next/link";

import { Button } from "@/components/ui/Button";
import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { FormField, Input } from "@/components/ui/Form";
import { OtpInput } from "@/components/ui/OtpInput";
import {
  requestResetCode,
  setNewPassword,
  verifyResetCode,
  type ResetState,
} from "./actions";

/**
 * The three patient reset steps in one component.
 *
 * The legacy app split them across forgot_password.html, otp_reset.html and
 * reset_password.html. Keeping them together means the flow advances without a
 * page reload and the verified email carries between steps without being round-
 * tripped through a query string the user could edit.
 *
 * Which step is showing is driven by the action results, not local state, so a
 * refresh mid-flow lands back on the step the server last confirmed.
 */
export function ForgotForm() {
  const [requested, requestAction] = useActionState<ResetState, FormData>(
    requestResetCode,
    undefined,
  );
  const [verified, verifyAction] = useActionState<ResetState, FormData>(
    verifyResetCode,
    undefined,
  );
  const [saved, saveAction] = useActionState<ResetState, FormData>(
    setNewPassword,
    undefined,
  );

  // --- Step 3: the new password was accepted ----------------------------
  if (saved?.done) {
    return (
      <div className="flex flex-col gap-4">
        <SuccessMessage>{saved.message}</SuccessMessage>
        <p className="text-sm text-text-muted">
          You can now sign in with your new password.
        </p>
        <Link href="/login" className="block">
          <Button fullWidth>Back to sign in</Button>
        </Link>
      </div>
    );
  }

  // --- Step 2: entering the emailed code --------------------------------
  // Reached once a code has been sent. verified.email confirms the code passed.
  if (verified?.email) {
    return (
      <form action={saveAction} className="flex flex-col gap-4">
        <input type="hidden" name="email" value={verified.email} />

        <p className="text-sm text-text-muted">
          Code verified for{" "}
          <span className="font-medium text-text-secondary">{verified.email}</span>.
          Choose a new password.
        </p>

        <FormField label="New password" htmlFor="password" required>
          <Input
            id="password"
            name="password"
            type="password"
            autoComplete="new-password"
            required
          />
        </FormField>

        <FormField label="Confirm new password" htmlFor="confirmPassword" required>
          <Input
            id="confirmPassword"
            name="confirmPassword"
            type="password"
            autoComplete="new-password"
            required
          />
        </FormField>

        {saved?.error ? <ErrorMessage>{saved.error}</ErrorMessage> : null}

        <Button type="submit" fullWidth disabled={Boolean(saved && !saved.error)}>
          {saved?.error ? "Try again" : "Set new password"}
        </Button>
      </form>
    );
  }

  // --- Step 2 (first pass): the code was sent, now verify it ------------
  if (requested?.email) {
    return (
      <form action={verifyAction} className="flex flex-col gap-4">
        <input type="hidden" name="email" value={requested.email} />

        <p className="text-sm text-text-muted">
          We sent a 6-digit code to{" "}
          <span className="font-medium text-text-secondary">{requested.email}</span>.
        </p>

        <OtpInput invalid={Boolean(verified?.error)} />

        {verified?.error ? (
          <ErrorMessage id="otp-error">{verified.error}</ErrorMessage>
        ) : null}

        <Button type="submit" fullWidth>
          Verify code
        </Button>

        <Link
          href="/forgot-password"
          className="text-center text-sm text-text-muted hover:text-primary-600"
        >
          Use a different email
        </Link>
      </form>
    );
  }

  // --- Step 1: request a code ------------------------------------------
  return (
    <form action={requestAction} className="flex flex-col gap-4">
      <FormField
        label="Email"
        htmlFor="email"
        required
        hint="We'll send a 6-digit verification code."
      >
        <Input
          id="email"
          name="email"
          type="email"
          autoComplete="username"
          required
          placeholder="you@example.com"
        />
      </FormField>

      {requested?.error ? <ErrorMessage>{requested.error}</ErrorMessage> : null}

      <Button type="submit" fullWidth>
        Send verification code
      </Button>

      <p className="text-center text-sm text-text-muted">
        <Link href="/login" className="hover:text-primary-600">
          Back to sign in
        </Link>
      </p>
    </form>
  );
}