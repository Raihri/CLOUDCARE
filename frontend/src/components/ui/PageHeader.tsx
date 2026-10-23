import type { ReactNode } from "react";

/** Consistent page title block. Replaces per-page ad-hoc headers. */
export function PageHeader({
  title,
  description,
  action,
}: {
  title: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <div className="flex flex-wrap items-start justify-between gap-3 pb-2">
      <div className="min-w-0">
        <h1 className="text-2xl font-semibold tracking-tight text-text-primary">
          {title}
        </h1>
        {description ? (
          <p className="mt-1 text-sm text-text-muted">{description}</p>
        ) : null}
      </div>
      {action ? (
        <div className="shrink-0 whitespace-nowrap">{action}</div>
      ) : null}
    </div>
  );
}