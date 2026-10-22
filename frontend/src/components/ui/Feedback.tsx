import type { ReactNode } from "react";

/**
 * Empty state — replaces the many ad-hoc "no data" blocks.
 * Every list in the app uses this so an empty result always looks intentional.
 */
export function EmptyState({
  title,
  description,
  action,
  icon = "◇",
}: {
  title: string;
  description?: string;
  action?: ReactNode;
  icon?: ReactNode;
}) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 px-6 py-14 text-center">
      <div className="flex size-11 items-center justify-center rounded-full bg-neutral-100 text-xl text-text-muted">
        {icon}
      </div>
      <div>
        <p className="font-medium text-text-primary">{title}</p>
        {description ? (
          <p className="mt-1 max-w-sm text-sm text-text-muted">{description}</p>
        ) : null}
      </div>
      {action}
    </div>
  );
}

export function Skeleton({ className = "" }: { className?: string }) {
  return (
    <div
      className={`animate-pulse rounded-md bg-neutral-200 ${className}`}
      aria-hidden
    />
  );
}

export function StatCard({
  label,
  value,
  hint,
  icon,
}: {
  label: string;
  value: ReactNode;
  hint?: string;
  icon?: ReactNode;
}) {
  return (
    <div className="bg-surface border border-border rounded-lg shadow-sm p-5">
      <div className="flex items-center justify-between">
        <p className="text-sm text-text-muted">{label}</p>
        {icon ? (
          <span className="flex size-8 items-center justify-center rounded-md bg-primary-50 text-primary-600">
            {icon}
          </span>
        ) : null}
      </div>
      <p className="mt-2 text-2xl font-semibold text-text-primary">{value}</p>
      {hint ? <p className="mt-1 text-xs text-text-muted">{hint}</p> : null}
    </div>
  );
}