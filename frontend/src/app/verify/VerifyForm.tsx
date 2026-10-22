"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/ui/Feedback";
import { OtpInput } from "@/components/ui/OtpInput";
import { verifyCode, type VerifyState } from "./actions";

export function VerifyForm({ email }: { email: string }) {
  const [state, formAction, pending] = useActionState<VerifyState, FormData>(
    verifyCode,
    undefined,
  );

  return (
    <form action={formAction} className="flex flex-col gap-4">
      <input type="hidden" name="email" value={email} />

      <OtpInput invalid={Boolean(state?.error)} />

      {state?.error ? (
        <ErrorMessage id="otp-error">{state.error}</ErrorMessage>
      ) : null}

      <Button type="submit" fullWidth disabled={pending}>
        {pending ? "Verifying…" : "Verify email"}
      </Button>
    </form>
  );
}