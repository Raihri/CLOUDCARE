"use client";

import Link from "next/link";
import { useActionState, useState, type ReactNode } from "react";

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
 * which is what the generic /login page wants — or `selectedRole` lets the
 * surrounding shell own the picker (and drive the role illustration) while
 * still submitting the same `role` field.
 */
export function LoginForm({
  action,
  next,
  role,
  selectedRole,
  initialEmail,
  notice,
  forgotHref,
  idPrefix = "login",
  submitLabel = "Sign in",
}: {
  action: (state: LoginState, data: FormData) => Promise<LoginState>;
  next?: string;
  role?: Role;
  /** Controlled role, used when the role picker lives outside this form. */
  selectedRole?: Role;
  /** Pre-fills the email after registration so the user only types a password. */
  initialEmail?: string;
  /** Optional success banner shown above the form. */
  notice?: ReactNode;
  forgotHref?: string;
  /** Prefixes the field IDs so two auth forms can coexist on one page. */
  idPrefix?: string;
  submitLabel?: string;
}) {
  const [state, formAction, pending] = useActionState(action, undefined);
  const [pickedRole, setPickedRole] = useState<Role>("PATIENT");

  // The selected role drives the route the Server Action posts to. Every
  // portal signs in with the same pair: email plus password.
  const activeRole = role ?? selectedRole ?? pickedRole;
  const controlled = Boolean(role || selectedRole);

  return (
    <form action={formAction} className="flex flex-col gap-4">
      {notice}
      {next ? <input type="hidden" name="next" value={next} /> : null}

      {controlled ? (
        // Locked portal, or a picker rendered by the surrounding auth shell.
        // The hidden field is still the value the Server Action reads.
        <input type="hidden" name="role" value={activeRole} />
      ) : (
        // Generic login: the select below is the only `role` field. A hidden
        // fallback here would be submitted first and would always win, which
        // made the picker silently sign everyone in as a patient.
        <FormField label="I am signing in as" htmlFor={`${idPrefix}-role`}>
          <Select
            id={`${idPrefix}-role`}
            name="role"
            value={activeRole}
            onChange={(event) => setPickedRole(event.target.value as Role)}
          >
            <option value="PATIENT">Patient / Donor</option>
            <option value="DOCTOR">Doctor</option>
            <option value="ADMIN">Administrator</option>
          </Select>
        </FormField>
      )}

      <FormField label="Email" htmlFor={`${idPrefix}-email`} required>
        <Input
          id={`${idPrefix}-email`}
          name="email"
          type="email"
          autoComplete="email"
          required
          defaultValue={initialEmail}
          placeholder="you@example.com"
        />
      </FormField>

      <FormField label="Password" htmlFor={`${idPrefix}-password`} required>
        <Input
          id={`${idPrefix}-password`}
          name="password"
          type="password"
          autoComplete="current-password"
          required
        />
      </FormField>

      {forgotHref ? (
        <p className="-mt-1 text-right text-xs">
          <Link
            href={forgotHref}
            className="font-medium text-text-muted transition-colors hover:text-primary-600"
          >
            Forgot your password?
          </Link>
        </p>
      ) : null}

      {state?.error ? <ErrorMessage>{state.error}</ErrorMessage> : null}

      <Button type="submit" fullWidth disabled={pending}>
        {pending ? "Signing in…" : submitLabel}
      </Button>
    </form>
  );
}
