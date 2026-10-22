"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";

export type VerifyState = { error?: string } | undefined;

/**
 * Submit the emailed 6-digit code.
 *
 * On success Spring marks the account enabled, authenticates the user and
 * persists the SecurityContext into the session — so the Set-Cookie we get
 * back here is a fully authenticated session and no second login is needed.
 */
export async function verifyCode(
  _prev: VerifyState,
  formData: FormData,
): Promise<VerifyState> {
  const email = String(formData.get("email") ?? "").trim();
  const digits = String(formData.get("code") ?? "").replace(/\D/g, "");

  if (!email) {
    return { error: "Email is required." };
  }
  if (digits.length !== 6) {
    return { error: "Enter the full 6-digit code." };
  }

  const base = process.env.SPRING_API_URL ?? "http://localhost:8080";

  const res = await fetch(`${base}/patient/otpverify`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      email,
      // OtpForm binds six separate fields named digit1..digit6; the
      // controller's buildOtpCode reassembles them into the full code.
      ...Object.fromEntries(
        Array.from({ length: 6 }, (_, i) => [
          `digit${i + 1}`,
          digits[i] ?? "",
        ]),
      ),
    }).toString(),
    redirect: "manual",
    cache: "no-store",
  });

  const setCookie = res.headers.get("set-cookie");
  const session = setCookie ? /JSESSIONID=[^;]+/.exec(setCookie)?.[0] : null;

  if (res.status === 302 && session) {
    const jar = await cookies();
    jar.set("JSESSIONID", session.split("=")[1], {
      httpOnly: true,
      sameSite: "lax",
      path: "/",
    });
    redirect("/dashboard");
  }

  const html = await res.text();
  const backendError = extractError(html);
  return { error: backendError ?? "Invalid or expired verification code." };
}

function extractError(html: string): string | null {
  const match =
    /errorMessage[^>]*>([\s\S]{0,300}?)<\/div>/i.exec(html) ??
    /class="error[^"]*"[^>]*>([\s\S]{0,300}?)<\/div>/i.exec(html);

  if (!match) return null;

  return match[1]
    .replace(/<[^>]+>/g, " ")
    .replace(/\s+/g, " ")
    .trim()
    .slice(0, 200);
}