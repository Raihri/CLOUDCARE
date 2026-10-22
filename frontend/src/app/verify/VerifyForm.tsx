"use client";

import { useActionState, useRef } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/ui/Feedback";
import { verifyCode, type VerifyState } from "./actions";

/**
 * Single OTP input implementation.
 *
 * The legacy app had three separate copies of this (index.html, otp.html,
 * otp_reset.html). Digits are collected into one hidden field, which the
 * Server Action splits back into the digit1..digit6 params OtpForm expects.
 */
export function VerifyForm({ email }: { email: string }) {
  const [state, formAction, pending] = useActionState<VerifyState, FormData>(
    verifyCode,
    undefined,
  );
  const codeRef = useRef<HTMLInputElement>(null);

  return (
    <form action={formAction} className="flex flex-col gap-4">
      <input type="hidden" name="email" value={email} />
      <input type="hidden" name="code" ref={codeRef} defaultValue="" />

      <div>
        <label
          htmlFor="otp"
          className="text-sm font-medium text-text-primary"
        >
          Verification code
        </label>

        <input
          id="otp"
          inputMode="numeric"
          autoComplete="one-time-code"
          maxLength={6}
          placeholder="······"
          aria-describedby={state?.error ? "otp-error" : undefined}
          onChange={(e) => {
            if (codeRef.current) {
              codeRef.current.value = e.target.value.replace(/\D/g, "").slice(0, 6);
            }
          }}
          className="mt-1.5 w-full rounded-md border border-border-strong bg-surface px-4 py-3 text-center text-2xl tracking-[0.5em] text-text-primary placeholder:text-text-muted focus:border-primary-500"
        />
      </div>

      {state?.error ? <ErrorMessage id="otp-error">{state.error}</ErrorMessage> : null}

      <Button type="submit" fullWidth disabled={pending}>
        {pending ? "Verifying…" : "Verify email"}
      </Button>
    </form>
  );
}