import type { HTMLAttributes, ReactNode } from "react";

export function Card({
  className = "",
  children,
  ...rest
}: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={[
        "bg-surface border border-border rounded-lg shadow-sm",
        className,
      ].join(" ")}
      {...rest}
    >
      {children}
    </div>
  );
}

export function CardHeader({
  title,
  description,
  action,
}: {
  title: ReactNode;
  description?: ReactNode;
  action?: ReactNode;
}) {
  return (
    <div className="flex items-start justify-between gap-4 px-5 py-4 border-b border-border">
      <div>
        <h2 className="text-base font-semibold text-text-primary">{title}</h2>
        {description ? (
          <p className="mt-0.5 text-sm text-text-muted">{description}</p>
        ) : null}
      </div>
      {action}
    </div>
  );
}

export function CardBody({
  className = "",
  children,
}: {
  className?: string;
  children: ReactNode;
}) {
  return <div className={`px-5 py-4 ${className}`}>{children}</div>;
}