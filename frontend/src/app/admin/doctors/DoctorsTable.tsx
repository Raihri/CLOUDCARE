import { StatusBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import type { AdminDoctor } from "@/lib/admin-api";
import { DoctorRowActions } from "./DoctorRowActions";

export function DoctorsTable({ doctors }: { doctors: AdminDoctor[] }) {
  if (doctors.length === 0) {
    return (
      <Card>
        <CardBody>
          <EmptyState
            icon={<i className="bx bxs-user" aria-hidden />}
            title="No doctors here"
            description="Applications will show up in Pending Doctors."
          />
        </CardBody>
      </Card>
    );
  }

  return (
    <Card>
      <CardBody>
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-border text-xs uppercase tracking-wide text-text-muted">
              <th className="pb-3 pr-4 font-medium">Doctor</th>
              <th className="pb-3 pr-4 font-medium">BMDC</th>
              <th className="pb-3 pr-4 font-medium">Specialization</th>
              <th className="pb-3 pr-4 font-medium">Hospital</th>
              <th className="pb-3 pr-4 font-medium">Status</th>
              <th className="pb-3 font-medium">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {doctors.map((d) => (
              <tr key={d.id}>
                <td className="py-3 pr-4">
                  <p className="font-medium text-text-primary">
                    {d.name ?? "—"}
                  </p>
                  <p className="text-xs text-text-muted">{d.email ?? ""}</p>
                </td>
                <td className="py-3 pr-4 text-text-secondary">
                  {d.bmdcRegNo ?? "—"}
                </td>
                <td className="py-3 pr-4 text-text-secondary">
                  {d.specialization ?? "—"}
                </td>
                <td className="py-3 pr-4 text-text-secondary">
                  {d.hospitalName ?? "—"}
                </td>
                <td className="py-3 pr-4">
                  <StatusBadge status={d.status} />
                </td>
                <td className="py-3">
                  <DoctorRowActions doctorId={d.id} status={d.status} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </CardBody>
    </Card>
  );
}