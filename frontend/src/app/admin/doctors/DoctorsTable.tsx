import { StatusBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import type { AdminDoctor } from "@/lib/admin-api";
import { DoctorRowActions } from "./DoctorRowActions";

function initials(name: string | null) {
  if (!name) return "?";
  return name
    .replace(/^Dr\.?\s+/i, "")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}

function DoctorAvatar({ name }: { name: string | null }) {
  return (
    <span className="flex size-9 shrink-0 items-center justify-center rounded-full bg-primary-50 text-xs font-semibold text-primary-700">
      {initials(name)}
    </span>
  );
}

export function DoctorsTable({
  doctors,
  emptyTitle = "No doctors here",
  emptyDescription = "Applications will show up in Pending Doctors.",
}: {
  doctors: AdminDoctor[];
  emptyTitle?: string;
  emptyDescription?: string;
}) {
  if (doctors.length === 0) {
    return (
      <Card>
        <CardBody>
          <EmptyState
            icon={<i className="bx bxs-user" aria-hidden />}
            title={emptyTitle}
            description={emptyDescription}
          />
        </CardBody>
      </Card>
    );
  }

  return (
    <Card className="overflow-hidden">
      {/* Phones get a card per doctor; a 900px-wide table is unusable there. */}
      <ul className="divide-y divide-border md:hidden">
        {doctors.map((doctor) => (
          <li key={doctor.id} className="p-4">
            <div className="flex items-start gap-3">
              <DoctorAvatar name={doctor.name} />

              <div className="min-w-0 flex-1">
                <div className="flex items-start justify-between gap-2">
                  <p className="min-w-0 truncate font-medium text-text-primary">
                    {doctor.name ?? "—"}
                  </p>
                  <StatusBadge status={doctor.status} />
                </div>
                <p className="truncate text-xs text-text-muted">
                  {doctor.email ?? ""}
                </p>

                <dl className="mt-3 grid grid-cols-2 gap-x-3 gap-y-2 text-xs">
                  <div>
                    <dt className="text-text-muted">BMDC</dt>
                    <dd className="mt-0.5 break-words text-text-primary">
                      {doctor.bmdcRegNo ?? "—"}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-text-muted">Specialization</dt>
                    <dd className="mt-0.5 break-words text-text-primary">
                      {doctor.specialization ?? "—"}
                    </dd>
                  </div>
                  <div className="col-span-2">
                    <dt className="text-text-muted">Hospital</dt>
                    <dd className="mt-0.5 break-words text-text-primary">
                      {doctor.hospitalName ?? "—"}
                    </dd>
                  </div>
                </dl>

                <div className="mt-3">
                  <DoctorRowActions
                    doctorId={doctor.id}
                    status={doctor.status}
                  />
                </div>
              </div>
            </div>
          </li>
        ))}
      </ul>

      {/* Tablet and up keeps the denser table. */}
      <div className="hidden overflow-x-auto md:block">
        <table className="w-full min-w-[880px] text-left text-sm">
          <thead>
            <tr className="border-b border-border bg-neutral-50/70 text-xs uppercase tracking-wide text-text-muted">
              <th className="px-5 py-3 font-medium">Doctor</th>
              <th className="px-4 py-3 font-medium">BMDC</th>
              <th className="px-4 py-3 font-medium">Specialization</th>
              <th className="px-4 py-3 font-medium">Hospital</th>
              <th className="px-4 py-3 font-medium">Status</th>
              <th className="px-5 py-3 font-medium">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {doctors.map((doctor) => (
              <tr
                key={doctor.id}
                className="transition-colors hover:bg-neutral-50/70"
              >
                <td className="px-5 py-3.5">
                  <div className="flex items-center gap-3">
                    <DoctorAvatar name={doctor.name} />
                    <div className="min-w-0">
                      <p className="truncate font-medium text-text-primary">
                        {doctor.name ?? "—"}
                      </p>
                      <p className="truncate text-xs text-text-muted">
                        {doctor.email ?? ""}
                      </p>
                    </div>
                  </div>
                </td>
                <td className="px-4 py-3.5 whitespace-nowrap text-text-secondary">
                  {doctor.bmdcRegNo ?? "—"}
                </td>
                <td className="px-4 py-3.5 whitespace-nowrap text-text-secondary">
                  {doctor.specialization ?? "—"}
                </td>
                <td className="px-4 py-3.5 whitespace-nowrap text-text-secondary">
                  {doctor.hospitalName ?? "—"}
                </td>
                <td className="px-4 py-3.5">
                  <StatusBadge status={doctor.status} />
                </td>
                <td className="px-5 py-3.5">
                  <DoctorRowActions
                    doctorId={doctor.id}
                    status={doctor.status}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  );
}
