import type {
  InputHTMLAttributes,
  ReactNode,
  SelectHTMLAttributes,
  TextareaHTMLAttributes,
} from "react";

/** Shared control styling. Every input in the app uses this one definition —
 *  the old codebase redefined label/input/focus/border on all 39 form pages. */
const CONTROL =
  "w-full rounded-md border border-border-strong bg-surface px-3 text-sm " +
  "text-text-primary placeholder:text-text-muted " +
  "transition-colors hover:border-neutral-400 " +
  "disabled:bg-neutral-100 disabled:text-text-muted disabled:cursor-not-allowed";

export function FormField({
  label,
  htmlFor,
  error,
  hint,
  required,
  children,
}: {
  label: string;
  htmlFor: string;
  error?: string;
  hint?: string;
  required?: boolean;
  children: ReactNode;
}) {
  return (
    <div className="flex flex-col gap-1.5">
      <label
        htmlFor={htmlFor}
        className="text-sm font-medium text-text-primary"
      >
        {label}
        {required ? <span className="text-danger ml-0.5">*</span> : null}
      </label>
      {children}
      {error ? (
        <p className="text-xs text-danger">{error}</p>
      ) : hint ? (
        <p className="text-xs text-text-muted">{hint}</p>
      ) : null}
    </div>
  );
}

export function Input({
  className = "",
  invalid,
  ...rest
}: InputHTMLAttributes<HTMLInputElement> & { invalid?: boolean }) {
  return (
    <input
      className={[CONTROL, "h-10", invalid ? "border-danger" : "", className].join(
        " ",
      )}
      aria-invalid={invalid || undefined}
      {...rest}
    />
  );
}

export function Textarea({
  className = "",
  invalid,
  ...rest
}: TextareaHTMLAttributes<HTMLTextAreaElement> & { invalid?: boolean }) {
  return (
    <textarea
      className={[
        CONTROL,
        "py-2 min-h-24 resize-y",
        invalid ? "border-danger" : "",
        className,
      ].join(" ")}
      aria-invalid={invalid || undefined}
      {...rest}
    />
  );
}

export function Select({
  className = "",
  invalid,
  children,
  ...rest
}: SelectHTMLAttributes<HTMLSelectElement> & { invalid?: boolean }) {
  return (
    <select
      className={[
        CONTROL,
        "h-10 pr-8 cursor-pointer",
        invalid ? "border-danger" : "",
        className,
      ].join(" ")}
      aria-invalid={invalid || undefined}
      {...rest}
    >
      {children}
    </select>
  );
}

export function Checkbox({
  label,
  className = "",
  ...rest
}: InputHTMLAttributes<HTMLInputElement> & { label: string }) {
  return (
    <label className={`flex items-center gap-2 cursor-pointer ${className}`}>
      <input
        type="checkbox"
        className="size-4 rounded border-border-strong accent-primary-600 cursor-pointer"
        {...rest}
      />
      <span className="text-sm text-text-secondary">{label}</span>
    </label>
  );
}