import type { ReactNode } from "react";

type Tone = "neutral" | "primary" | "success" | "warning" | "danger" | "info";

const TONES: Record<Tone, string> = {
  neutral: "bg-neutral-100 text-text-secondary border-neutral-200",
  primary: "bg-primary-50 text-primary-700 border-primary-200",
  success: "bg-success-soft text-success border-success/20",
  warning: "bg-warning-soft text-warning border-warning/20",
  danger: "bg-danger-soft text-danger border-danger/20",
  info: "bg-info-soft text-info border-info/20",
};

export function Badge({
  tone = "neutral",
  children,
  className = "",
}: {
  tone?: Tone;
  children: ReactNode;
  className?: string;
}) {
  return (
    <span
      className={[
        "inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5",
        "text-xs font-medium whitespace-nowrap",
        TONES[tone],
        className,
      ].join(" ")}
    >
      {children}
    </span>
  );
}

/**
 * Blood group indicator.
 *
 * This is the ONE legitimate use of the restricted `--color-blood` red, along
 * with urgency badges and critical alerts. Blood type is meaningful red — it
 * should read instantly and is never used for anything else in the product.
 */
export function BloodGroupBadge({ group }: { group?: string | null }) {
  if (!group || group === "Unknown") {
    return <Badge tone="neutral">Unknown</Badge>;
  }
  return (
    <span className="inline-flex items-center justify-center rounded-full bg-blood-soft border border-blood/25 px-3 py-1 text-sm font-semibold text-blood">
      {group}
    </span>
  );
}

/**
 * Maps a request/donation/appointment status to a consistent tone.
 *
 * Two different vocabularies reach this component: blood requests use
 * "Pending"/"Matched"/"Confirmed" while appointments use uppercase
 * "PENDING"/"CONFIRMED"/"CANCELLED"/"COMPLETED". Matching is
 * case-insensitive so one mapping covers both.
 */
export function StatusBadge({ status }: { status?: string | null }) {
  const key = status?.toUpperCase();

  const tone: Tone =
    key === "PENDING"
      ? "warning"
      : key === "MATCHED"
        ? "info"
        : key === "CONFIRMED" || key === "COMPLETED" || key === "APPROVED"
          ? "success"
          : key === "CANCELLED" || key === "REJECTED" || key === "NOT ELIGIBLE"
            ? "danger"
            : "neutral";

  return <Badge tone={tone}>{status ?? "Unknown"}</Badge>;
}