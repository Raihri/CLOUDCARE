"use server";

import { springJson } from "@/lib/api";

/**
 * Fetch a short-lived ticket for the notifications WebSocket.
 *
 * Runs on the server with the user's forwarded session cookie, so Spring only
 * issues a ticket for whoever is actually signed in. The browser presents it
 * in the STOMP CONNECT frame; it is valid for two minutes.
 */
export async function getWebSocketTicket(): Promise<string | null> {
  try {
    const { ticket } = await springJson<{ ticket: string }>("/api/ws-ticket");
    return ticket ?? null;
  } catch {
    return null;
  }
}
