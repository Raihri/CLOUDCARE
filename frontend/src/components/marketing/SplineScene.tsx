"use client";

import { useEffect, useRef, useState } from "react";

/**
 * Spline scene slot for the landing-page hero.
 *
 * The scene loaded here (`IlsQUG57tmd1X4R3`) was remixed from a Spline
 * community file and published from the user's own Spline account — the
 * original community scene id (`a38eafa0-…`) never returns a working runtime
 * URL because an unpublished remix always 403s; this one does, verified with
 * a plain GET (200, real .splinecode payload).
 *
 * ── Why this component is defensive, even for a working scene ───────────
 * Loading can still fail for reasons that have nothing to do with the scene
 * itself: no WebGL (old hardware, remote desktops, hardened browsers),
 * offline, or the user's own `prefers-reduced-motion` setting. The loader
 * treats any of those as a NORMAL outcome, not an exception: it unmounts
 * quietly and leaves whatever the page already rendered underneath.
 *
 * Consequences of that rule, all deliberate:
 *   • No spinners, no error text, no broken-image icon. A failure must look
 *     identical to the scene simply not being there.
 *   • No console noise for a visitor who did nothing wrong.
 *   • `pointer-events-none` until the scene reports it is actually running, so
 *     an invisible canvas can never swallow clicks meant for the CTAs behind
 *     it — the most likely way this feature could quietly break the page.
 *
 * To use a different scene, change SCENE_URL — nothing else needs touching.
 */

const SCENE_URL =
  "https://prod.spline.design/IlsQUG57tmd1X4R3/scene.splinecode";

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
    // Declared here (not inside the try block) so the cleanup closure below
    // can remove it from the DOM regardless of where loading got to.
    let canvas: HTMLCanvasElement | undefined;

    // WebGL is unavailable in plenty of real situations (old hardware, remote
    // desktops, hardened browsers, reduced-motion setups). Spline throws on
    // construction there, so this catches the constructor as well as the load.
    try {
      // Feature-detect WebGL on a THROWAWAY canvas, never the real one. A
      // canvas can only ever bind one context type — calling getContext here
      // on the canvas Application will use would permanently lock it to
      // WebGL, and this runtime prefers WebGPU when available. Testing on a
      // separate canvas keeps the real one untouched so Spline can pick
      // whichever backend it wants.
      const probe = document.createElement("canvas");
      const gl =
        probe.getContext("webgl2") ??
        probe.getContext("webgl") ??
        probe.getContext("experimental-webgl");

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

      canvas = document.createElement("canvas");
      canvas.className = "size-full";
      host.appendChild(canvas);
      // Narrowed to a plain local once assigned: `canvas` (the closure-shared,
      // possibly-undefined one) exists so cleanup can always find it, but the
      // load chain below only ever runs after this exact assignment.
      const loadingCanvas = canvas;

      void import("@splinetool/runtime")
        .then(({ Application }) => {
          if (disposed || !hostRef.current) return;

          app = new Application(loadingCanvas);
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
      canvas?.remove();
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