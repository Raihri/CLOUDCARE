"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/ui/Feedback";
import { FormField, Input } from "@/components/ui/Form";
import { register, type RegisterState } from "./actions";

export function RegisterForm({
  /** Prefixes the field IDs so the sign-in form can share the page. */
  idPrefix = "register",
}: {
  idPrefix?: string;
}) {
  const [state, formAction, pending] = useActionState<RegisterState, FormData>(
    register,
    undefined,
  );

  return (
    <form action={formAction} className="flex flex-col gap-4">
      <FormField label="Full name" htmlFor={`${idPrefix}-name`} required>
        <Input
          id={`${idPrefix}-name`}
          name="name"
          autoComplete="name"
          required
        />
      </FormField>

      <FormField label="Email" htmlFor={`${idPrefix}-email`} required>
        <Input
          id={`${idPrefix}-email`}
          name="email"
          type="email"
          autoComplete="email"
          required
          placeholder="you@example.com"
        />
      </FormField>

      <FormField label="Password" htmlFor={`${idPrefix}-password`} required>
        <Input
          id={`${idPrefix}-password`}
          name="password"
          type="password"
          autoComplete="new-password"
          required
          minLength={6}
        />
      </FormField>

      <FormField
        label="Confirm password"
        htmlFor={`${idPrefix}-confirmPassword`}
        required
      >
        <Input
          id={`${idPrefix}-confirmPassword`}
          name="confirmPassword"
          type="password"
          autoComplete="new-password"
          required
          minLength={6}
        />
      </FormField>

      {state?.error ? <ErrorMessage>{state.error}</ErrorMessage> : null}

      <Button type="submit" fullWidth disabled={pending}>
        {pending ? "Creating account…" : "Create account"}
      </Button>
    </form>
  );
}
