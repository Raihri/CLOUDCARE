import type { Role } from "@/lib/auth";

export type NavItem = {
  href: string;
  label: string;
  icon: string;
  exact?: boolean;
};

/**
 * One nav definition per role, replacing ~17 hand-rolled sidebars that each
 * had their own icon set, link order and active-state logic.
 */
export const NAV_BY_ROLE: Record<Role, NavItem[]> = {
  PATIENT: [
    { href: "/dashboard", label: "Dashboard", icon: "▦", exact: true },
    { href: "/requests", label: "Blood Requests", icon: "❥" },
    { href: "/donors", label: "Donors", icon: "◈" },
    { href: "/doctors", label: "Doctors", icon: "⚕" },
    { href: "/appointments", label: "Appointments", icon: "☰" },
    { href: "/health-records", label: "Health Records", icon: "❤" },
    { href: "/ai-chat", label: "CloudCare AI", icon: "✦" },
    { href: "/quiz", label: "Mental Health Quiz", icon: "◑" },
    { href: "/notifications", label: "Notifications", icon: "◔" },
    { href: "/feedback", label: "Feedback", icon: "✎" },
  ],
  DOCTOR: [
    { href: "/doctor/dashboard", label: "Dashboard", icon: "▦", exact: true },
    { href: "/doctor/appointments", label: "Appointments", icon: "☰" },
    { href: "/doctor/availability", label: "Availability", icon: "◷" },
    { href: "/doctor/patients", label: "Patients", icon: "◈" },
    { href: "/doctor/profile", label: "My Profile", icon: "⚕" },
  ],
  ADMIN: [
    { href: "/admin/dashboard", label: "Dashboard", icon: "▦", exact: true },
    { href: "/admin/doctors", label: "Doctors", icon: "⚕" },
    { href: "/admin/doctors/pending", label: "Pending Approval", icon: "◷" },
    { href: "/admin/donors", label: "Donor Analytics", icon: "◈" },
    { href: "/admin/feedbacks", label: "Feedback", icon: "✎" },
  ],
};

/** Which nav tree a given area belongs to, so the sidebar highlights correctly. */
export function roleForPath(pathname: string): Role | null {
  if (pathname.startsWith("/doctor")) return "DOCTOR";
  if (pathname.startsWith("/admin")) return "ADMIN";
  if (
    pathname === "/" ||
    pathname.startsWith("/login") ||
    pathname.startsWith("/register")
  ) {
    return null;
  }
  return "PATIENT";
}

export function isActive(item: NavItem, pathname: string): boolean {
  return item.exact
    ? pathname === item.href
    : pathname === item.href || pathname.startsWith(`${item.href}/`);
}