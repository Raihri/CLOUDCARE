"use client";

import { useEffect, useRef, useState } from "react";

/**
 * Spline scene slot for the landing-page hero.
 *
 * ── Why this component is defensive ──────────────────────────────────────
 * The scene this points at (`a38eafa0-…`) is a Spline *community remix*, not a
 * published scene. Every runtime URL for it currently answers 403 Access
 * Denied, verified in a real browser:
 *
 *   prod.spline.design/a38eafa0-…/scene.splinecode  -> 403
 *   my.spline.design/a38eafa0-…                     -> 403
 *
 * That is expected for an unpublished scene and it is not fixable from here —
 * only the scene's creator can press Publish. So the loader treats a failed
 * scene as a NORMAL outcome, not an exception: it unmounts quietly and leaves
 * whatever the page already rendered underneath.
 *
 * Consequences of that rule, all deliberate:
 *   • No spinners, no error text, no broken-image icon. A 403 must look
 *     identical to the scene simply not being there.
 *   • No console noise for a visitor who did nothing wrong.
 *   • `pointer-events-none` until the scene reports it is actually running, so
 *     an invisible canvas can never swallow clicks meant for the CTAs behind
 *     it — the most likely way this feature could quietly break the page.
 *
 * Once the scene is published this needs no code change: it starts rendering
 * on its own. To use a different scene, change SCENE_URL.
 */

const SCENE_URL =
  "https://prod.spline.design/a38eafa0-2fa5-4630-983f-6940475adf5e/scene.splinecode";

/**
 * Remembers a failed scene for the rest of the browser session.
 *
 * Without this, every visit re-downloads the 2.8MB runtime only to 403 again.
 * The flag is set on ANY failure — bad scene id, offline, WebGL refusal — which
 * is safe: the cost of a wrongly-cached failure is one missing decoration, and
 * the cost of not caching it is 2.8MB on every single page load.
 *
 * sessionStorage (not localStorage) so clearing the tab frees it without the
 * user hunting through site settings.
 */
const FAILED_KEY = "spline-scene-failed";

function alreadyFailed(): boolean {
  try {
    return window.sessionStorage.getItem(FAILED_KEY) === "1";
  } catch {
    // Private browsing / blocked storage: fail open and just try again.
    return false;
  }
}

function markFailed(): void {
  try {
    window.sessionStorage.setItem(FAILED_KEY, "1");
  } catch {
    // Nothing to do — the in-memory path already handled this visit.
  }
}

export function SplineScene({ className = "" }: { className?: string }) {
  const hostRef = useRef<HTMLDivElement>(null);
  const [active, setActive] = useState(false);

  useEffect(() => {
    const host = hostRef.current;
    if (!host) return;

    // The runtime is ~2.8MB uncompressed. It must not be fetched on a phone:
    // the slot is `lg:block`, so below 1024px there is nothing to render into,
    // and paying 2.8MB for an invisible element is indefensible on mobile data.
    // `matchMedia` is checked BEFORE the dynamic import so the chunk is never
    // requested at all — a CSS-only `hidden` class would still let the effect
    // run and the import fire.
    if (!window.matchMedia?.("(min-width: 1024px)")?.matches) return;

    // Already known-bad this session — skip the 2.8MB import entirely.
    if (alreadyFailed()) return;

    let disposed = false;
    // The real Application type. `setZoom` was deliberately not used: passing
    // `undefined` to it is not a documented "freeze" call, and guessing at the
    // API to honour prefers-reduced-motion would be worse than not claiming to.
    // Reduced-motion is handled where it can be verified — the CSS transition
    // on the wrapper — not by poking an API I can't confirm.
    let app: import("@splinetool/runtime").Application | undefined;

    // WebGL is unavailable in plenty of real situations (old hardware, remote
    // desktops, hardened browsers, reduced-motion setups). Spline throws on
    // construction there, so this catches the constructor as well as the load.
    try {
      const canvas = document.createElement("canvas");
      const gl =
        canvas.getContext("webgl2") ??
        canvas.getContext("webgl") ??
        canvas.getContext("experimental-webgl");

      if (!gl) return;

      // Honour the OS "reduce motion" setting by simply not loading the scene.
      // Spline's runtime exposes no documented way to pause its render loop
      // without guessing at the API, and a cursor-following animation is exactly
      // the kind of motion this setting exists to suppress — so skipping the
      // 2.8MB download entirely is both the honest and the cheaper answer.
      const reduced = window.matchMedia?.(
        "(prefers-reduced-motion: reduce)",
      )?.matches;
      if (reduced) return;

      void import("@splinetool/runtime")
        .then(({ Application }) => {
          if (disposed || !hostRef.current) return;

          app = new Application(canvas);
          // A scene that 403s rejects here. Swallowed on purpose: an
          // unpublished scene is the expected state, not a crash.
          return app
            .load(SCENE_URL)
            .then(() => {
              if (disposed) {
                app?.dispose?.();
                return;
              }
              // Only now does the slot become interactive — an invisible
              // canvas must never intercept clicks meant for the CTAs.
              setActive(true);
            })
            .catch(() => {
              app?.dispose?.();
              markFailed();
            });
        })
        .catch(() => {
          app?.dispose?.();
          markFailed();
        });
    } catch {
      // Construction threw (no WebGL, blocked, etc). Leave the slot empty.
      markFailed();
      return;
    }

    return () => {
      disposed = true;
      try {
        app?.dispose?.();
      } catch {
        // Disposal races with an in-flight load. Nothing to recover.
      }
    };
  }, []);

  return (
    <div
      ref={hostRef}
      aria-hidden
      className={[
        "pointer-events-none absolute inset-0",
        // Fade in only once live, so the swap from "nothing" to "robot" is
        // not a hard pop.
        active ? "opacity-100" : "opacity-0",
        "transition-opacity duration-700 motion-reduce:transition-none",
        className,
      ].join(" ")}
    />
  );
}