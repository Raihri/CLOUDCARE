"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { FormField, Input, Select } from "@/components/ui/Form";
import type { LoginState } from "./actions";

/**
 * One login form for all three roles. The old app had three near-identical
 * login pages (index.html, doctor_login.html, admin_login.html), each with
 * its own styling.
 */
export function LoginForm({
  action,
  next,
}: {
  action: (state: LoginState, data: FormData) => Promise<LoginState>;
  next?: string;
}) {
  const [state, formAction, pending] = useActionState(action, undefined);

  return (
    <form action={formAction} className="flex flex-col gap-4">
      {next ? <input type="hidden" name="next" value={next} /> : null}

      <FormField label="I am signing in as" htmlFor="role">
        <Select id="role" name="role" defaultValue="PATIENT">
          <option value="PATIENT">Patient / Donor</option>
          <option value="DOCTOR">Doctor</option>
          <option value="ADMIN">Administrator</option>
        </Select>
      </FormField>

      <FormField label="Email" htmlFor="email" required>
        <Input
          id="email"
          name="email"
          type="email"
          autoComplete="username"
          required
          placeholder="you@example.com"
        />
      </FormField>

      <FormField label="Password" htmlFor="password" required>
        <Input
          id="password"
          name="password"
          type="password"
          autoComplete="current-password"
          required
        />
      </FormField>

      {state?.error ? (
        <p
          role="alert"
          className="rounded-md border border-danger/20 bg-danger-soft px-3 py-2 text-sm text-danger"
        >
          {state.error}
        </p>
      ) : null}

      <Button type="submit" fullWidth disabled={pending}>
        {pending ? "Signing in…" : "Sign in"}
      </Button>
    </form>
  );
}