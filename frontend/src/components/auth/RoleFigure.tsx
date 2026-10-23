"use client";

import { useId } from "react";

import type { Role } from "@/lib/auth";

/**
 * The three portal illustrations.
 *
 * They are deliberately drawn as flat, theme-token SVGs rather than shipped as
 * raster art: the palette stays in `globals.css`, the figures scale cleanly from
 * the mobile banner to the desktop overlay, and there is no extra image request
 * on the most important page in the product.
 *
 * Each role gets the same calm silhouette in a different clinical context —
 * coat and stethoscope, everyday patient, operations blazer and shield — so the
 * portal is legible before the copy is read.
 */
export function RoleFigure({
  role,
  className = "",
}: {
  role: Role;
  className?: string;
}) {
  const rawId = useId();
  const uid = rawId.replace(/[^a-zA-Z0-9_-]/g, "");

  const coat = `${uid}-coat`;
  const shirt = `${uid}-shirt`;
  const hair = `${uid}-hair`;

  return (
    <svg
      viewBox="0 0 360 420"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
      className={className}
    >
      <defs>
        <linearGradient id={coat} x1="108" y1="198" x2="254" y2="350" gradientUnits="userSpaceOnUse">
          <stop stopColor="var(--color-surface)" />
          <stop offset="1" stopColor="var(--color-primary-100)" />
        </linearGradient>
        <linearGradient id={shirt} x1="168" y1="206" x2="194" y2="318" gradientUnits="userSpaceOnUse">
          <stop stopColor="var(--color-primary-600)" />
          <stop offset="1" stopColor="var(--color-primary-800)" />
        </linearGradient>
        <linearGradient id={hair} x1="142" y1="84" x2="220" y2="146" gradientUnits="userSpaceOnUse">
          <stop stopColor="var(--color-primary-800)" />
          <stop offset="1" stopColor="var(--color-primary-900)" />
        </linearGradient>
      </defs>

      {/* Halo + ground shadow: the figure reads as a portrait, not a sticker. */}
      <g opacity="0.55">
        <circle
          cx="180"
          cy="198"
          r="150"
          stroke="var(--color-text-inverted)"
          strokeOpacity="0.16"
          strokeDasharray="3 11"
        />
        <circle cx="180" cy="198" r="119" fill="var(--color-text-inverted)" fillOpacity="0.045" />
        <circle
          cx="180"
          cy="198"
          r="101"
          stroke="var(--color-text-inverted)"
          strokeOpacity="0.09"
        />
      </g>
      <ellipse cx="180" cy="360" rx="94" ry="13" fill="var(--color-primary-900)" opacity="0.24" />

      {/* Neck + head are shared, which keeps the three illustrations a family. */}
      <rect x="166" y="164" width="28" height="38" rx="13" fill="var(--color-primary-100)" />
      <circle cx="180" cy="128" r="38" fill="var(--color-surface)" />
      <circle cx="142" cy="131" r="5" fill="var(--color-primary-100)" />
      <circle cx="218" cy="131" r="5" fill="var(--color-primary-100)" />
      <path
        d="M142 124C142 96 157 82 180 82C203 82 218 96 218 124C209 113 197 108 180 108C163 108 151 113 142 124Z"
        fill={`url(#${hair})`}
      />

      {role === "DOCTOR" ? (
        <DoctorBody coat={coat} shirt={shirt} />
      ) : role === "ADMIN" ? (
        <AdminBody shirt={shirt} />
      ) : (
        <PatientBody coat={coat} shirt={shirt} />
      )}

      {role === "DOCTOR" ? <DoctorProps /> : null}
      {role === "PATIENT" ? <PatientProps /> : null}
      {role === "ADMIN" ? <AdminProps /> : null}
    </svg>
  );
}

function DoctorBody({ coat, shirt }: { coat: string; shirt: string }) {
  return (
    <g>
      {/* White coat with a soft blue turn-down. */}
      <path
        d="M108 346V292C108 238 136 203 180 203C224 203 252 238 252 292V346H108Z"
        fill={`url(#${coat})`}
      />
      <path
        d="M153 204L180 251L207 204C199 199 190 196 180 196C170 196 161 199 153 204Z"
        fill={`url(#${shirt})`}
      />
      <path d="M153 204L180 251L166 302L139 219Z" fill="var(--color-surface)" />
      <path d="M207 204L180 251L194 302L221 219Z" fill="var(--color-primary-100)" />
      <path
        d="M180 251V346"
        stroke="var(--color-primary-200)"
        strokeWidth="2"
        strokeDasharray="4 6"
      />

      {/* Stethoscope. */}
      <path
        d="M149 214C136 247 142 274 164 283"
        stroke="var(--color-primary-800)"
        strokeWidth="6"
        strokeLinecap="round"
      />
      <path
        d="M211 214C224 247 218 274 196 283"
        stroke="var(--color-primary-800)"
        strokeWidth="6"
        strokeLinecap="round"
      />
      <path
        d="M164 283C173 292 187 292 196 283"
        stroke="var(--color-primary-800)"
        strokeWidth="6"
        strokeLinecap="round"
      />
      <circle cx="180" cy="294" r="10" fill="var(--color-primary-600)" />
      <circle cx="180" cy="294" r="4" fill="var(--color-surface)" />

      {/* Cross badge + pocket. */}
      <rect x="132" y="252" width="27" height="19" rx="5" fill="var(--color-primary-600)" />
      <path d="M145.5 256V267M140 261.5H151" stroke="var(--color-surface)" strokeWidth="2" strokeLinecap="round" />
      <path d="M209 254H232V274" stroke="var(--color-primary-300)" strokeWidth="2" strokeLinecap="round" />
      <path d="M215 260H226" stroke="var(--color-primary-300)" strokeWidth="2" strokeLinecap="round" />

      {/* Arms read as one continuous silhouette at small sizes. */}
      <path d="M112 290C104 300 102 316 104 334" stroke="var(--color-primary-200)" strokeWidth="10" strokeLinecap="round" />
      <path d="M248 290C256 300 258 316 256 334" stroke="var(--color-primary-200)" strokeWidth="10" strokeLinecap="round" />
    </g>
  );
}

function PatientBody({ coat, shirt }: { coat: string; shirt: string }) {
  return (
    <g>
      {/* A relaxed hoodie keeps the patient soft and everyday. */}
      <path
        d="M108 346V290C108 238 138 203 180 203C222 203 252 238 252 290V346H108Z"
        fill={`url(#${coat})`}
      />
      <path
        d="M143 204C151 186 163 177 180 177C197 177 209 186 217 204C204 214 194 218 180 218C166 218 156 214 143 204Z"
        fill="var(--color-primary-200)"
      />
      <path
        d="M154 206L180 240L206 206C197 199 190 196 180 196C170 196 163 199 154 206Z"
        fill={`url(#${shirt})`}
      />
      <path d="M154 206L180 240L168 270L144 220Z" fill="var(--color-primary-100)" opacity="0.9" />
      <path d="M206 206L180 240L192 270L216 220Z" fill="var(--color-primary-100)" opacity="0.9" />

      {/* Heart emblem. */}
      <path
        d="M180 286C180 286 157 272 157 257C157 248 164 243 171 243C176 243 179 246 180 249C181 246 184 243 189 243C196 243 203 248 203 257C203 272 180 286 180 286Z"
        fill="var(--color-mint)"
      />

      {/* Drawstrings and a pocket. */}
      <path d="M168 238V260M192 238V260" stroke="var(--color-primary-300)" strokeWidth="2" strokeLinecap="round" />
      <path d="M154 300H206" stroke="var(--color-primary-200)" strokeWidth="3" strokeLinecap="round" />
      <path d="M112 292C104 301 102 316 104 333" stroke="var(--color-primary-200)" strokeWidth="10" strokeLinecap="round" />
      <path d="M248 292C256 301 258 316 256 333" stroke="var(--color-primary-200)" strokeWidth="10" strokeLinecap="round" />
    </g>
  );
}

function AdminBody({ shirt }: { shirt: string }) {
  return (
    <g>
      {/* Structured blazer; darker than the clinical coats. */}
      <path
        d="M106 346V292C106 237 136 202 180 202C224 202 254 237 254 292V346H106Z"
        fill="var(--color-primary-800)"
      />
      <path
        d="M152 204L180 252L208 204C199 199 190 196 180 196C170 196 161 199 152 204Z"
        fill={`url(#${shirt})`}
      />
      <path d="M152 204L180 252L163 306L132 222Z" fill="var(--color-primary-700)" />
      <path d="M208 204L180 252L197 306L228 222Z" fill="var(--color-primary-900)" />
      <path d="M180 252V346" stroke="var(--color-primary-700)" strokeWidth="2" />

      {/* Shirt + tie. */}
      <path
        d="M163 210L180 238L197 210C190 205 186 203 180 203C174 203 170 205 163 210Z"
        fill="var(--color-surface)"
        opacity="0.95"
      />
      <path d="M180 238L174 252L180 304L186 252L180 238Z" fill="var(--color-highlight)" />

      {/* Lanyard + access badge. */}
      <path
        d="M164 213L151 268M196 213L209 268"
        stroke="var(--color-primary-600)"
        strokeWidth="3"
        strokeLinecap="round"
      />
      <rect x="132" y="266" width="32" height="22" rx="5" fill="var(--color-surface)" />
      <rect x="136" y="270" width="24" height="7" rx="3" fill="var(--color-primary-600)" />
      <rect x="136" y="280" width="16" height="3" rx="1.5" fill="var(--color-primary-200)" />

      {/* Pocket square and arms. */}
      <path d="M211 260H234V282" stroke="var(--color-primary-600)" strokeWidth="2" strokeLinecap="round" />
      <path d="M110 292C102 301 100 316 102 334" stroke="var(--color-primary-700)" strokeWidth="10" strokeLinecap="round" />
      <path d="M250 292C258 301 260 316 258 334" stroke="var(--color-primary-700)" strokeWidth="10" strokeLinecap="round" />
    </g>
  );
}

function DoctorProps() {
  return (
    <g>
      <g className="auth-float">
        <rect x="52" y="76" width="68" height="58" rx="13" fill="var(--color-surface)" />
        <rect x="52" y="76" width="68" height="19" rx="9" fill="var(--color-primary-600)" />
        <rect x="63" y="105" width="19" height="5" rx="2.5" fill="var(--color-primary-200)" />
        <rect x="63" y="115" width="33" height="5" rx="2.5" fill="var(--color-primary-100)" />
        <circle cx="105" cy="116" r="7" fill="var(--color-mint)" />
      </g>
      <g className="auth-float-delayed">
        <rect x="260" y="247" width="60" height="44" rx="12" fill="var(--color-surface)" />
        <path
          d="M269 270H280L285 260L291 281L296 266L301 270H311"
          stroke="var(--color-primary-600)"
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </g>
    </g>
  );
}

function PatientProps() {
  return (
    <g>
      <g className="auth-float">
        <circle cx="282" cy="92" r="27" fill="var(--color-surface)" />
        <path
          d="M282 105C282 105 269 97 269 89C269 84 273 81 277 81C280 81 282 83 282 85C282 83 284 81 287 81C291 81 295 84 295 89C295 97 282 105 282 105Z"
          fill="var(--color-mint)"
        />
      </g>
      <g className="auth-float-delayed">
        <rect x="40" y="242" width="62" height="62" rx="16" fill="var(--color-surface)" />
        <path
          d="M71 255C81 255 89 263 89 273C89 284 71 294 71 294C71 294 53 284 53 273C53 263 61 255 71 255Z"
          fill="var(--color-primary-100)"
        />
        <path
          d="M71 262C76 262 79 266 79 270C79 276 71 281 71 281C71 281 63 276 63 270C63 266 66 262 71 262Z"
          fill="var(--color-primary-600)"
        />
      </g>
    </g>
  );
}

function AdminProps() {
  return (
    <g>
      <g className="auth-float">
        <circle cx="282" cy="92" r="30" fill="var(--color-surface)" />
        <path
          d="M282 76L293 80V89C293 97 288 103 282 106C276 103 271 97 271 89V80L282 76Z"
          fill="var(--color-primary-600)"
        />
        <path
          d="M277 90L281 94L288 86"
          stroke="var(--color-surface)"
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </g>
      <g className="auth-float-delayed">
        <rect x="40" y="244" width="64" height="54" rx="13" fill="var(--color-surface)" />
        <rect x="51" y="269" width="9" height="17" rx="2" fill="var(--color-primary-200)" />
        <rect x="65" y="260" width="9" height="26" rx="2" fill="var(--color-primary-400)" />
        <rect x="79" y="252" width="9" height="34" rx="2" fill="var(--color-primary-600)" />
        <path d="M51 257H91" stroke="var(--color-primary-100)" strokeWidth="3" strokeLinecap="round" />
      </g>
    </g>
  );
}
