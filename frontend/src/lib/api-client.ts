/**
 * Browser-side fetch to Spring, through the same-origin proxy.
 *
 * Only needed for interactions that must mutate without a full Server Action
 * round-trip (booking a slot from the list). It targets `/api/spring/*`, which
 * next.config.ts rewrites to the backend, so the session cookie stays
 * first-party and no CORS preflight happens. Calling the backend's :8080
 * directly from the browser would be cross-origin and fail.
 */
export async function springFetch(
  path: string,
  init: RequestInit = {},
): Promise<Response> {
  return fetch(`/api/spring${path}`, {
    ...init,
    credentials: "same-origin",
    headers: {
      ...(init.body ? { "Content-Type": "application/json" } : {}),
      ...init.headers,
    },
  });
}