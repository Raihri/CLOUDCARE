"use server";

import { ApiError, springJson } from "@/lib/api";

export type ChatState = {
  id: number;
  question: string;
  reply: string;
  ai: boolean;
  error?: string;
} | undefined;

/** Increments per response so the client can tell a new result from a re-render
 *  of the previous one — useActionState replays state on every render. */
let counter = 0;

export async function sendMessage(
  _prev: ChatState,
  formData: FormData,
): Promise<ChatState> {
  const message = String(formData.get("message") ?? "").trim();
  if (!message) return undefined;

  try {
    const res = await springJson<{ reply: string; ai: boolean }>(
      "/patient/api/chat",
      {
        method: "POST",
        body: new URLSearchParams({ message }).toString(),
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
      },
    );
    return {
      id: ++counter,
      question: message,
      reply: res?.reply ?? "I didn't get a reply. Please try rephrasing that.",
      ai: Boolean(res?.ai),
    };
  } catch (err) {
    return {
      id: ++counter,
      question: message,
      reply: "",
      ai: false,
      error:
        err instanceof ApiError
          ? err.message
          : "Could not reach the assistant. Please try again.",
    };
  }
}