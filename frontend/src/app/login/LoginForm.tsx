"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/ui/Feedback";
import { FormField, Input, Select } from "@/components/ui/Form";
import type { Role } from "@/lib/auth";
import type { LoginState } from "./actions";

/**
 * One login form for all three roles. The old app had three near-identical
 * login pages (index.html, doctor_login.html, admin_login.html), each with
 * its own styling.
 *
 * `role` locks the form to one portal. Without it the form shows a picker,
 * which is what the generic /login page wants.
 */
export function LoginForm({
  action,
  next,
  role,
}: {
  action: (state: LoginState, data: FormData) => Promise<LoginState>;
  next?: string;
  role?: Role;
}) {
  const [state, formAction, pending] = useActionState(action, undefined);

  return (
    <form action={formAction} className="flex flex-col gap-4">
      {next ? <input type="hidden" name="next" value={next} /> : null}
      {/* Always submitted so the Server Action knows which Spring endpoint to
          post to, even when the picker is hidden. */}
      <input type="hidden" name="role" value={role ?? "PATIENT"} />

      {role ? (
        <input type="hidden" name="role" value={role} />
      ) : (
        <FormField label="I am signing in as" htmlFor="role">
          <Select id="role" name="role" defaultValue="PATIENT">
            <option value="PATIENT">Patient / Donor</option>
            <option value="DOCTOR">Doctor</option>
            <option value="ADMIN">Administrator</option>
          </Select>
        </FormField>
      )}

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

      {state?.error ? <ErrorMessage>{state.error}</ErrorMessage> : null}

      <Button type="submit" fullWidth disabled={pending}>
        {pending ? "Signing in…" : "Sign in"}
      </Button>
    </form>
  );
}