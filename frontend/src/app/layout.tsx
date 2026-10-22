import type { Metadata } from "next";
import { Poppins } from "next/font/google";

import "./globals.css";

/* ONE font for the entire product, chosen to MATCH the existing templates —
   style.css imports Poppins 300-700 and every dashboard inherits it. The old
   codebase had 15 competing stacks (Poppins vs Inter vs Montserrat vs Arial
   vs Courier); this locks in the dominant one. next/font self-hosts it, so
   there is no external request and no layout shift. */
const poppins = Poppins({
  variable: "--font-poppins",
  subsets: ["latin"],
  weight: ["300", "400", "500", "600", "700"],
  display: "swap",
});

export const metadata: Metadata = {
  title: {
    default: "CloudCare — Your Health Companion",
    template: "%s | CloudCare",
  },
  description:
    "Blood donation, doctor appointments and health records in one place.",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en" className={`${poppins.variable} h-full antialiased`}>
      <head>
        {/* Boxicons is the icon set every template already uses
            (bxs-droplet, bx-list-ul, bxs-user, ...). Ported rather than
            replaced so the nav keeps the icons it has always had. */}
        <link
          rel="stylesheet"
          href="https://unpkg.com/boxicons@2.0.7/css/boxicons.min.css"
        />
      </head>
      <body className="min-h-full flex flex-col font-sans">{children}</body>
    </html>
  );
}