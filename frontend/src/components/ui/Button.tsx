import type { ButtonHTMLAttributes, ReactNode } from "react";

type Variant = "primary" | "secondary" | "ghost" | "danger" | "blood";
type Size = "sm" | "md" | "lg";

type Props = Omit<ButtonHTMLAttributes<HTMLButtonElement>, "type"> & {
  /**
   * Either a button behaviour ("submit" | "reset" | "button") or a Server
   * Action. React 19 lets a button submit a specific action directly, which
   * is the only way to have a submit button OUTSIDE its own <form> — the
   * escape hatch for forms that can't be nested (e.g. a photo upload card
   * living above a personal-details form on the same page).
   */
  type?: ButtonHTMLAttributes<HTMLButtonElement>["type"] | ((formData: FormData) => void | Promise<void>);
  variant?: Variant;
  size?: Size;
  fullWidth?: boolean;
  children?: ReactNode;
};

/* Values ported from static/css/style.css: primary buttons use navy #040D72,
   which is the colour the templates already use for `.btn`/headings. */
const VARIANTS: Record<Variant, string> = {
  primary:
    "bg-primary-900 text-white hover:bg-primary-800 active:bg-primary-900 shadow-sm",
  secondary:
    "bg-surface text-primary-900 border border-primary-600 hover:bg-primary-50",
  ghost: "bg-transparent text-text-secondary hover:bg-neutral-200",
  danger: "bg-danger text-white hover:bg-danger/90",
  // Reserved for blood-donation actions only.
  blood: "bg-blood text-white hover:bg-blood-dark",
};

const SIZES: Record<Size, string> = {
  sm: "h-8 px-3 text-sm",
  md: "h-10 px-4 text-sm",
  lg: "h-12 px-6 text-base",
};

export function Button({
  variant = "primary",
  size = "md",
  fullWidth = false,
  className = "",
  children,
  type = "button",
  ...rest
}: Props) {
  // A Server Action function can't go through the `type` attribute, so it's
  // applied via formAction instead. These are mutually exclusive.
  const isAction = typeof type === "function";
  return (
    <button
      {...(isAction
        ? { formAction: type as (formData: FormData) => void }
        : { type: type as "submit" | "reset" | "button" })}
      className={[
        "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md font-medium",
        "transition-colors disabled:opacity-50 disabled:cursor-not-allowed",
        VARIANTS[variant],
        SIZES[size],
        fullWidth ? "w-full" : "",
        className,
      ].join(" ")}
      {...rest}
    >
      {children}
    </button>
  );
}