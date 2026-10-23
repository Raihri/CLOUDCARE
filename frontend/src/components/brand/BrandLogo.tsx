import Image from "next/image";

/**
 * The CloudCare brand mark.
 *
 * The supplied `logo.png` is a square lock-up (icon + wordmark), so it works
 * as a self-contained tile at small sizes. The adjacent text in the sidebar
 * and landing header still carries the name for legibility; this is the face
 * of the brand, not a replacement for the wordmark.
 */
export function BrandLogo({
  size = 40,
  className = "",
  priority = false,
}: {
  size?: number;
  className?: string;
  /** Set on above-the-fold marks (sidebar, auth card) to avoid a late swap. */
  priority?: boolean;
}) {
  return (
    <span
      className={[
        "inline-flex shrink-0 items-center justify-center overflow-hidden rounded-xl bg-white",
        className,
      ].join(" ")}
      style={{ width: size, height: size }}
    >
      <Image
        src="/logo.png"
        alt=""
        width={size}
        height={size}
        priority={priority}
        className="size-full object-contain"
      />
    </span>
  );
}
