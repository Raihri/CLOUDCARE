"use client";

import { useRef } from "react";

/**
 * Single OTP field implementation.
 *
 * The legacy app carried three separate copies of this control (index.html,
 * otp.html, otp_reset.html). Non-digits are stripped and input is capped at 6
 * characters so the submitted value is always a clean 6-digit code — the shape
 * the backend JSON endpoints now expect, instead of the six separate
 * digit1..digit6 params that only existed to feed the old six-box widget.
 *
 * Renders a hidden field carrying the real value so this works inside a plain
 * <form> and submits without any JavaScript coordination.
 */
export function OtpInput({
  name = "code",
  label = "Verification code",
  id = "otp",
  invalid = false,
}: {
  name?: string;
  label?: string;
  id?: string;
  /** Wires up aria-describedby when the parent is showing an error. */
  invalid?: boolean;
}) {
  const codeRef = useRef<HTMLInputElement>(null);

  return (
    <div>
      <label htmlFor={id} className="text-sm font-medium text-text-primary">
        {label}
      </label>

      {/* The submitted value lives here; the visible box is presentational. */}
      <input type="hidden" name={name} ref={codeRef} defaultValue="" />

      <input
        id={id}
        inputMode="numeric"
        autoComplete="one-time-code"
        maxLength={6}
        placeholder="······"
        aria-describedby={invalid ? `${id}-error` : undefined}
        aria-invalid={invalid || undefined}
        onChange={(e) => {
          if (codeRef.current) {
            codeRef.current.value = e.target.value.replace(/\D/g, "").slice(0, 6);
          }
        }}
        className="mt-1.5 w-full rounded-md border border-border-strong bg-surface px-4 py-3 text-center text-2xl tracking-[0.5em] text-text-primary placeholder:text-text-muted focus:border-primary-500"
      />
    </div>
  );
}