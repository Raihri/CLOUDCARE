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
  // FormData must be left alone: the browser derives its own multipart
  // Content-Type with a boundary. Setting application/json here would make
  // JSON.stringify the body — silently turning any File into {} — and Spring
  // would then see no parts at all.
  const isFormData = typeof FormData !== "undefined" && init.body instanceof FormData;

  return fetch(`/api/spring${path}`, {
    ...init,
    credentials: "same-origin",
    headers: {
      ...(init.body && !isFormData ? { "Content-Type": "application/json" } : {}),
      ...init.headers,
    },
  });
}