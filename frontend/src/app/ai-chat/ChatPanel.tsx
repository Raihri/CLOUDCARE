"use client";

import { useActionState, useEffect, useRef, useState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/ui/Feedback";
import { sendMessage, type ChatState } from "./actions";

type Message = { role: "user" | "bot"; text: string };

const GREETING =
  "Hi! I'm your CloudCare assistant. Ask me about fever, cough, headaches, blood pressure, diabetes or stomach issues — or anything else health-related.";

/**
 * Chat UI ported from templates/aichat.html.
 *
 * <p>Conversation is kept in local component state and the backend is
 * stateless per request, so there is no server round-trip on render — only the
 * message post itself hits Spring.
 */
export function ChatPanel({ displayName }: { displayName: string }) {
  const [state, formAction, pending] = useActionState<ChatState, FormData>(
    sendMessage,
    undefined,
  );

  const [messages, setMessages] = useState<Message[]>(() => [
    { role: "bot", text: GREETING },
  ]);

  const scrollRef = useRef<HTMLDivElement>(null);
  const lastHandled = useRef(0);

  // The action resolves with the reply; fold it into local state. The counter
  // guard keeps a re-render from replaying the same result twice.
  useEffect(() => {
    if (!state?.reply || state.id === lastHandled.current) return;
    lastHandled.current = state.id;
    setMessages((prev) => [
      ...prev,
      { role: "user", text: state.question ?? "" },
      { role: "bot", text: state.reply },
    ]);
  }, [state]);

  useEffect(() => {
    scrollRef.current?.scrollTo({
      top: scrollRef.current.scrollHeight,
      behavior: "smooth",
    });
  }, [messages.length, pending]);

  return (
    <div className="flex h-[calc(100vh-220px)] min-h-125 flex-col overflow-hidden rounded-lg border border-border bg-surface shadow-sm">
      <div className="flex items-center gap-3 border-b border-border bg-navy px-5 py-4">
        <span className="flex h-10 w-10 items-center justify-center rounded-full bg-azure/20 text-xl text-white">
          <i className="bx bxs-bot" aria-hidden />
        </span>
        <div className="min-w-0">
          <h2 className="text-sm font-semibold text-white">
            CloudCare AI Assistant
          </h2>
          <p className="truncate text-xs text-white/60">
            {state?.ai
              ? "Answering with GPT-4o mini"
              : "Guided health information — always confirm with a doctor"}
          </p>
        </div>
      </div>

      <div ref={scrollRef} className="flex-1 space-y-4 overflow-y-auto p-5">
        {messages.map((m, i) => (
          <div
            key={i}
            className={m.role === "user" ? "flex justify-end" : "flex justify-start"}
          >
            <div
              className={
                m.role === "user"
                  ? "max-w-[75%] rounded-lg rounded-br-sm bg-navy px-4 py-2.5 text-sm whitespace-pre-wrap text-white"
                  : "max-w-[75%] rounded-lg rounded-bl-sm border border-border bg-neutral-50 px-4 py-2.5 text-sm whitespace-pre-wrap text-text-primary"
              }
            >
              {m.text}
            </div>
          </div>
        ))}
        {pending ? (
          <div className="flex justify-start">
            <div className="rounded-lg rounded-bl-sm border border-border bg-neutral-50 px-4 py-2.5 text-sm text-text-muted">
              Typing…
            </div>
          </div>
        ) : null}
      </div>

      <form
        action={formAction}
        className="flex items-center gap-3 border-t border-border p-4"
      >
        <input type="hidden" name="name" value={displayName} />
        <input
          name="message"
          required
          autoComplete="off"
          placeholder="Ask about a symptom, medication or appointment…"
          className="h-10 flex-1 rounded-md border border-border-strong bg-surface px-3 text-sm text-text-primary placeholder:text-text-muted focus:border-azure focus:outline-none"
        />
        <Button type="submit" disabled={pending}>
          <i className="bx bxs-send" aria-hidden /> {pending ? "…" : "Send"}
        </Button>
      </form>

      {state?.error ? (
        <div className="px-4 pb-4">
          <ErrorMessage>{state.error}</ErrorMessage>
        </div>
      ) : null}
    </div>
  );
}