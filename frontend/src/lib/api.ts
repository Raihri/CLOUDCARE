import "server-only";

import { cookies } from "next/headers";

const SPRING_API_URL =
  process.env.SPRING_API_URL ?? "http://localhost:8080";

/** The Spring session cookie. Auth state lives entirely in the SecurityContext,
 *  which Spring persists here. Without forwarding it, every call 401s. */
const SESSION_COOKIE = "JSESSIONID";

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
    readonly body?: unknown,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

/**
 * Build the Cookie header from the incoming request so Spring sees the same
 * session. This is the single point where auth is preserved.
 */
async function cookieHeader(): Promise<string> {
  const jar = await cookies();
  const session = jar.get(SESSION_COOKIE)?.value;
  return session ? `${SESSION_COOKIE}=${session}` : "";
}

/**
 * Server-side fetch to Spring.
 *
 * Notes that matter:
 *  - `redirect: "manual"` so a Spring 302 (formLogin success, or a redirect
 *    view) does NOT get followed. Following it would return the HTML of the
 *    destination page instead of the JSON we asked for, which fails confusingly.
 *    Callers inspect `res.redirected`/`res.headers.get("location")` instead.
 *  - Cookie jar is forwarded, never a full browser cookie string.
 */
export async function springFetch(
  path: string,
  init: RequestInit & { auth?: boolean } = {},
): Promise<Response> {
  const { auth = true, headers, ...rest } = init;

  const cookie = auth ? await cookieHeader() : "";

  return fetch(`${SPRING_API_URL}${path}`, {
    ...rest,
    headers: {
      ...(rest.body instanceof FormData || rest.body instanceof URLSearchParams
        ? {}
        : { "Content-Type": "application/json" }),
      ...(cookie ? { Cookie: cookie } : {}),
      ...headers,
    },
    redirect: "manual",
    cache: "no-store",
  });
}

/** springFetch + JSON parse + non-2xx -> ApiError. */
export async function springJson<T>(
  path: string,
  init: RequestInit & { auth?: boolean } = {},
): Promise<T> {
  const res = await springFetch(path, init);

  const text = await res.text();
  const data = text ? safeParse(text) : null;

  if (!res.ok) {
    throw new ApiError(
      res.status,
      extractMessage(data) ?? `Request failed (${res.status})`,
      data,
    );
  }
  return data as T;
}

function safeParse(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    // Spring returned HTML — almost always a login redirect or a server error
    // page. Surfacing this as a parse failure would hide the real cause.
    return { raw: text.slice(0, 500) };
  }
}

/** Spring's existing JSON envelope is {success, message, data}. Use its
 *  `message` when present so backend validation text reaches the user. */
function extractMessage(data: unknown): string | undefined {
  if (data && typeof data === "object") {
    const msg = (data as Record<string, unknown>).message;
    if (typeof msg === "string" && msg.trim()) return msg;
  }
  return undefined;
}

/**
 * Unwrap the list envelope used by the existing Spring JSON APIs.
 *
 * The two families disagree on the key: RequestController returns
 * `{success, count, data}`, while DonorController returns
 * `{success, count, donors}`. Accept both rather than assuming one.
 */
export function unwrap<T>(envelope: unknown): T[] {
  if (Array.isArray(envelope)) return envelope as T[];

  if (envelope && typeof envelope === "object") {
    const record = envelope as Record<string, unknown>;
    for (const key of ["data", "donors", "requests", "doctors"]) {
      const value = record[key];
      if (Array.isArray(value)) return value as T[];
    }
    // A single-object payload ("data": {...}) still yields a one-item list.
    if (record.data && typeof record.data === "object") {
      return [record.data as T];
    }
  }
  return [];
}