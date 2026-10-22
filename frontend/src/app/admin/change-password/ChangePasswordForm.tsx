"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { FormField, Input } from "@/components/ui/Form";
import { changeAdminPassword, type ChangePasswordState } from "./actions";

/** Replaces admin_change_password.html. */
export function ChangePasswordForm() {
  const [state, formAction, pending] = useActionState<ChangePasswordState, FormData>(
    changeAdminPassword,
    undefined,
  );

  return (
    <form action={formAction} className="flex max-w-md flex-col gap-4">
      <FormField label="Current password" htmlFor="oldPassword" required>
        <Input
          id="oldPassword"
          name="oldPassword"
          type="password"
          autoComplete="current-password"
          required
        />
      </FormField>

      <FormField
        label="New password"
        htmlFor="newPassword"
        required
        hint="At least 6 characters."
      >
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

      {state?.error ? <ErrorMessage>{state.error}</ErrorMessage> : null}
      {state?.message && !state.error ? (
        <SuccessMessage>{state.message}</SuccessMessage>
      ) : null}

      <div>
        <Button type="submit" disabled={pending}>
          {pending ? "Updating…" : "Change password"}
        </Button>
      </div>
    </form>
  );
}