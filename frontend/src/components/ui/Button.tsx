import type { ButtonHTMLAttributes, ReactNode } from "react";

type Variant = "primary" | "secondary" | "ghost" | "danger" | "blood";
type Size = "sm" | "md" | "lg";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
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
  return (
    <button
      type={type}
      className={[
        "inline-flex items-center justify-center gap-2 rounded-md font-medium",
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