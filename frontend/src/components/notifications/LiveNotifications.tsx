"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import type { Client, IMessage } from "@stomp/stompjs";

import { getWebSocketTicket } from "@/lib/ws-ticket";

type ConnectionState = "connecting" | "live" | "offline";

type LiveNotification = {
  id?: number;
  message?: string;
  date?: string;
};

const STATUS_LABEL: Record<ConnectionState, string> = {
  connecting: "Connecting…",
  live: "Live updates",
  offline: "Reconnecting…",
};

const STATUS_DOT: Record<ConnectionState, string> = {
  connecting: "bg-warning",
  live: "bg-success",
  offline: "bg-danger",
};

/**
 * Live notification bridge for the patient portal.
 *
 * Spring exposes a SockJS/STOMP endpoint at `/ws`. Before every (re)connect we
 * fetch a fresh, signed, two-minute ticket through a Server Action (which
 * forwards the session cookie) and send it in the STOMP CONNECT frame.
 * `WebSocketConfig` verifies it and binds the connection to that user, so the
 * subscription to `/user/queue/notifications` only ever receives our own
 * messages. The client never asserts its own identity.
 *
 * The toast is deliberately small and dismissible: the authoritative list is
 * still `/notifications`, which the server renders from the database.
 */
export function LiveNotifications({ email }: { email: string }) {
  const [status, setStatus] = useState<ConnectionState>("connecting");
  const [latest, setLatest] = useState<LiveNotification | null>(null);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function connect() {
      try {
        const [{ Client }, sockJsModule] = await Promise.all([
          import("@stomp/stompjs"),
          import("sockjs-client"),
        ]);
        if (cancelled) return;

        const SockJS = sockJsModule.default;
        const base =
          process.env.NEXT_PUBLIC_SPRING_WS_URL ??
          `${process.env.NEXT_PUBLIC_SPRING_URL ?? "http://localhost:8080"}/ws`;

        const client = new Client({
          webSocketFactory: () => new SockJS(base) as unknown as WebSocket,
          // Runs before the first connect and every reconnect: tickets are
          // short-lived, so each attempt gets a fresh one.
          beforeConnect: async (c) => {
            // Without a ticket the server refuses CONNECT and the client
            // retries after reconnectDelay, which is the behaviour we want
            // for a transient failure.
            const ticket = await getWebSocketTicket();
            c.connectHeaders = ticket ? { ticket } : {};
          },
          reconnectDelay: 5000,
          heartbeatIncoming: 10000,
          heartbeatOutgoing: 10000,
          debug: () => {},
          onConnect: () => {
            if (cancelled) return;
            setStatus("live");
            client.subscribe("/user/queue/notifications", (message: IMessage) => {
              try {
                const payload = JSON.parse(message.body) as LiveNotification;
                setLatest(payload);
              } catch {
                setLatest({ message: message.body });
              }
            });
          },
          onWebSocketClose: () => {
            if (!cancelled) setStatus("offline");
          },
          onStompError: () => {
            if (!cancelled) setStatus("offline");
          },
        });

        clientRef.current = client;
        await client.activate();
      } catch {
        if (!cancelled) setStatus("offline");
      }
    }

    connect();

    return () => {
      cancelled = true;
      const client = clientRef.current;
      clientRef.current = null;
      if (client) {
        void client.deactivate();
      }
    };
  }, [email]);

  return (
    <>
      <div className="flex items-center gap-2 px-2 pb-1 text-[11px] text-white/45">
        <span
          aria-hidden
          className={`size-1.5 rounded-full ${STATUS_DOT[status]}`}
        />
        <span>{STATUS_LABEL[status]}</span>
      </div>

      {latest ? (
        <div
          role="status"
          className="fixed right-4 bottom-4 z-[60] w-[min(360px,calc(100vw-2rem))] rounded-xl border border-border bg-surface p-4 shadow-xl"
        >
          <div className="flex items-start gap-3">
            <span className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
              <i className="bx bxs-bell text-lg" aria-hidden />
            </span>
            <div className="min-w-0 flex-1">
              <p className="text-xs font-semibold uppercase tracking-wide text-text-muted">
                New notification
              </p>
              <p className="mt-1 text-sm text-text-primary">
                {latest.message ?? "Your record was updated."}
              </p>
              <Link
                href="/notifications"
                className="mt-2 inline-block text-xs font-medium text-primary-600 hover:underline"
              >
                View all notifications →
              </Link>
            </div>
            <button
              type="button"
              aria-label="Dismiss notification"
              onClick={() => setLatest(null)}
              className="-mt-1 -mr-1 flex size-7 shrink-0 items-center justify-center rounded-md text-text-muted hover:bg-neutral-100"
            >
              <i className="bx bx-x text-lg" aria-hidden />
            </button>
          </div>
        </div>
      ) : null}
    </>
  );
}
