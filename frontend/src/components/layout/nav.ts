import type { Role } from "@/lib/auth";

export type NavItem = {
  href: string;
  label: string;
  /** Boxicons class, ported from fragments/sidebar.html so the nav keeps the
   *  icons it has always had. */
  icon: string;
  exact?: boolean;
};

/**
 * Ported from src/main/resources/templates/fragments/sidebar.html — the 8
 * patient entries, their labels and their exact `bx bxs-*` icons. Hrefs point
 * at the new Next.js routes rather than the old Thymeleaf paths.
 *
 * Doctor and admin trees were hand-rolled per page in the legacy templates;
 * these consolidate them using the same visual language.
 */
export const NAV_BY_ROLE: Record<Role, NavItem[]> = {
  PATIENT: [
    { href: "/dashboard", label: "Patient Profile", icon: "bxs-user", exact: true },
    { href: "/health-records", label: "Edit Profile", icon: "bxs-edit" },
    { href: "/doctors", label: "Doctors List", icon: "bx-list-ul" },
    { href: "/donors", label: "Find Donor", icon: "bxs-droplet" },
    { href: "/donor-dashboard", label: "Donor Dashboard", icon: "bxs-dashboard" },
    { href: "/feedback", label: "Patient Feedback", icon: "bxs-star" },
    { href: "/appointments", label: "Appointments", icon: "bxs-calendar-check" },
  ],
  DOCTOR: [
    { href: "/doctor/dashboard", label: "Dashboard", icon: "bxs-user", exact: true },
    { href: "/doctor/appointments", label: "Appointments", icon: "bxs-calendar-check" },
    { href: "/doctor/availability", label: "Availability", icon: "bx-time" },
    { href: "/doctor/patients", label: "My Patients", icon: "bx-list-ul" },
    { href: "/doctor/profile", label: "Edit Profile", icon: "bxs-edit" },
  ],
  ADMIN: [
    { href: "/admin/dashboard", label: "Dashboard", icon: "bxs-dashboard", exact: true },
    { href: "/admin/doctors", label: "Doctors List", icon: "bx-list-ul" },
    { href: "/admin/doctors/pending", label: "Pending Doctors", icon: "bxs-time" },
    { href: "/admin/donors", label: "Donor Analysis", icon: "bxs-droplet" },
    { href: "/admin/feedbacks", label: "Feedback List", icon: "bxs-star" },
    { href: "/admin/change-password", label: "Change Password", icon: "bxs-lock-alt" },
  ],
};

/** Which nav tree a given area belongs to, so the sidebar highlights correctly. */
export function roleForPath(pathname: string): Role | null {
  if (pathname.startsWith("/doctor")) return "DOCTOR";
  if (pathname.startsWith("/admin")) return "ADMIN";
  if (
    pathname === "/" ||
    pathname.startsWith("/login") ||
    pathname.startsWith("/register") ||
    pathname.startsWith("/verify")
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