import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

/* ONE font for the entire product. Replaces the old codebase's 15 competing
   font stacks (Poppins vs Inter vs Montserrat vs Arial vs Courier).
   next/font self-hosts it — no external request, no layout shift. */
const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
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
    <html lang="en" className={`${inter.variable} h-full antialiased`}>
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}