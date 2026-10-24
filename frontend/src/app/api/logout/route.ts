import { cookies } from "next/headers";
import { SPRING_API_URL } from "@/lib/api";

/**
 * Sign out.
 *
 * Spring Security's logout URL only accepts POST, so the browser posts here
 * and this handler relays to Spring with the session cookie attached.
 * Note the old admin_control.html used a GET <a href="/admin/logout">, which
 * Spring silently rejected.
 */
export async function POST(request: Request) {
  const base = SPRING_API_URL;
  const jar = await cookies();
  const session = jar.get("JSESSIONID")?.value;

  // Best-effort: clear the server session if we can, but never block logout on
  // it. A stale server session is recoverable; a user stuck signed in is not.
  try {
    await fetch(`${base}/patient/logout`, {
      method: "POST",
      headers: session ? { Cookie: `JSESSIONID=${session}` } : {},
      redirect: "manual",
      cache: "no-store",
    });
  } catch {
    // Backend unreachable — proceed with clearing the local cookie.
  }

  jar.delete("JSESSIONID");

  return Response.redirect(new URL("/", request.url), 303);
}