import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";

import { AppShell } from "@/components/layout/AppShell";
import { BloodGroupBadge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { BookSlotButton } from "@/components/patient/BookSlotButton";
import { fetchAvailability, fetchDoctor } from "@/lib/patient-api";

export const metadata: Metadata = { title: "Doctor Profile" };

export const instant = false;

export default async function DoctorProfilePage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const user = await requireRole(["PATIENT"], `/doctors/${id}`);
  const doctorId = Number(id);

  if (!Number.isFinite(doctorId)) notFound();

  const doctor = await fetchDoctor(doctorId);
  if (!doctor) notFound();

  const slots = await fetchAvailability(doctorId);

  const facts: Array<[string, string | null | undefined]> = [
    ["Specialization", doctor.specialization],
    ["Degrees", doctor.degrees],
    ["Experience", doctor.experienceYears ? `${doctor.experienceYears} years` : null],
    ["Hospital", doctor.hospitalName],
    ["Location", [doctor.zilla, doctor.address].filter(Boolean).join(", ")],
    ["Phone", doctor.phoneNumber],
    ["Email", doctor.email],
    ["Languages", doctor.languages],
    ["Working days", doctor.workingDays],
    ["Working hours", doctor.workingHours],
  ];

  return (
    <AppShell user={user}>
      <PageHeader
        title={doctor.name}
        description={doctor.specialization || "General Medicine"}
        action={
          <Link href="/doctors">
            <Button variant="secondary" size="sm">
              ← Back to list
            </Button>
          </Link>
        }
      />

      <div className="grid grid-cols-3 gap-4">
        {/* ---- Details ---- */}
        <div className="col-span-2 flex flex-col gap-4">
          <Card>
            <CardHeader title="About" />
            <CardBody>
              {doctor.description ? (
                <p className="whitespace-pre-line text-sm leading-relaxed text-text-secondary">
                  {doctor.description}
                </p>
              ) : (
                <p className="text-sm text-text-muted">
                  This doctor hasn&apos;t added a profile description yet.
                </p>
              )}
            </CardBody>
          </Card>

          <Card>
            <CardHeader title="Details" />
            <CardBody>
              <dl className="grid grid-cols-2 gap-x-6 gap-y-3 text-sm">
                {facts.map(([label, value]) => (
                  <div key={label}>
                    <dt className="text-xs uppercase tracking-wide text-text-muted">
                      {label}
                    </dt>
                    <dd className="mt-0.5 text-text-primary">{value || "—"}</dd>
                  </div>
                ))}
              </dl>
            </CardBody>
          </Card>
        </div>

        {/* ---- Booking ---- */}
        <div className="flex flex-col gap-4">
          <Card>
            <CardBody className="flex flex-col gap-3">
              <div className="flex items-center justify-between">
                <span className="text-sm text-text-muted">
                  Consultation fee
                </span>
                <span className="text-lg font-semibold text-text-primary">
                  {doctor.consultationFee ? `৳${doctor.consultationFee}` : "—"}
                </span>
              </div>

              {doctor.rating ? (
                <div className="flex items-center justify-between">
                  <span className="text-sm text-text-muted">Rating</span>
                  <span className="flex items-center gap-1 text-sm text-text-primary">
                    <i className="bx bxs-star text-warning" aria-hidden />
                    {doctor.rating}
                  </span>
                </div>
              ) : null}

              {doctor.bloodGroup ? (
                <div className="flex items-center justify-between">
                  <span className="text-sm text-text-muted">Blood group</span>
                  <BloodGroupBadge group={doctor.bloodGroup} />
                </div>
              ) : null}

              {doctor.telemedicineAvailable ? (
                <p className="flex items-center gap-1.5 text-sm text-success">
                  <i className="bx bxs-video" aria-hidden />
                  Telemedicine available
                </p>
              ) : null}
            </CardBody>
          </Card>

          <Card>
            <CardHeader
              title="Available slots"
              description="Only unbooked times are listed."
            />
            <CardBody>
              {slots.length === 0 ? (
                <EmptyState
                  icon={<i className="bx bx-time" aria-hidden />}
                  title="No open slots"
                  description="This doctor has no bookable times right now. Check back later."
                />
              ) : (
                <ul className="flex flex-col gap-2">
                  {slots.map((slot) => (
                    <li
                      key={slot.id}
                      className="flex items-center justify-between gap-3 rounded-md border border-border p-3"
                    >
                      <div>
                        <p className="text-sm font-medium text-text-primary">
                          {slot.startTime?.slice(0, 5)} –{" "}
                          {slot.endTime?.slice(0, 5)}
                        </p>
                        <p className="text-xs text-text-muted">
                          {slot.date}
                          {slot.telemedicineAvailable ? " · Online" : ""}
                        </p>
                      </div>
                      <BookSlotButton doctorId={doctorId} slotId={slot.id} />
                    </li>
                  ))}
                </ul>
              )}
            </CardBody>
          </Card>
        </div>
      </div>
    </AppShell>
  );
}