"use client";

import { useActionState } from "react";
import Link from "next/link";

import { Button } from "@/components/ui/Button";
import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { FormField, Input } from "@/components/ui/Form";
import {
  requestDoctorReset,
  setDoctorPassword,
  type DoctorResetState,
} from "./actions";

/**
 * Doctor reset, replacing doctor_forgot_password.html and
 * doctor_reset_password.html.
 *
 * The backend echoes the generated token back on the request so the link step
 * works without a real mailbox in development. In production the token only
 * arrives by email and /doctor/reset-password?token=… carries it instead.
 */
export function DoctorForgotForm({ initialToken = "" }: { initialToken?: string }) {
  const [requested, requestAction] = useActionState<DoctorResetState, FormData>(
    requestDoctorReset,
    initialToken ? { token: initialToken } : undefined,
  );
  const [saved, saveAction] = useActionState<DoctorResetState, FormData>(
    setDoctorPassword,
    undefined,
  );

  if (saved?.done) {
    return (
      <div className="flex flex-col gap-4">
        <SuccessMessage>{saved.message}</SuccessMessage>
        <p className="text-sm text-text-muted">
          You can now sign in with your new password.
        </p>
        <Link href="/doctor/login" className="block">
          <Button fullWidth>Back to sign in</Button>
        </Link>
      </div>
    );
  }

  if (requested?.token) {
    return (
      <form action={saveAction} className="flex flex-col gap-4">
        <input type="hidden" name="token" value={requested.token} />

        <SuccessMessage>{requested.message}</SuccessMessage>

        <FormField label="New password" htmlFor="newPassword" required>
          <Input
            id="newPassword"
            name="newPassword"
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

        <Button type="submit" fullWidth>
          Set new password
        </Button>

        <Link
          href="/doctor/forgot-password"
          className="text-center text-sm text-text-muted hover:text-primary-600"
        >
          Use a different email
        </Link>
      </form>
    );
  }

  return (
    <form action={requestAction} className="flex flex-col gap-4">
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

      {requested?.error ? <ErrorMessage>{requested.error}</ErrorMessage> : null}

      <Button type="submit" fullWidth>
        Send reset link
      </Button>

      <p className="text-center text-sm text-text-muted">
        <Link href="/doctor/login" className="hover:text-primary-600">
          Back to sign in
        </Link>
      </p>
    </form>
  );
}