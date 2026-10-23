import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { StatusBadge } from "@/components/ui/Badge";
import { Card, CardBody } from "@/components/ui/Card";
import { ErrorMessage } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchDoctorProfile } from "@/lib/doctor-api";
import { ProfileForm } from "./ProfileForm";

export const metadata: Metadata = { title: "Doctor Profile" };

export const instant = false;

export default async function DoctorProfilePage() {
  const user = await requireRole(["DOCTOR"], "/doctor/profile");
  const profile = await fetchDoctorProfile();

  return (
    <AppShell user={user}>
      <PageHeader
        title="My Profile"
        description="How patients see you when they browse doctors."
      />

      {!profile ? (
        <Card>
          <CardBody>
            <ErrorMessage>We could not load your profile. Please refresh and try again.</ErrorMessage>
          </CardBody>
        </Card>
      ) : (
        <>
          <Card className="mb-5">
            <CardBody>
              <div className="flex items-start gap-5">
                {profile.profileImage ? (
                  <img
                    src={profile.profileImage}
                    alt=""
                    className="h-24 w-24 rounded-lg object-cover"
                  />
                ) : (
                  <div className="flex h-24 w-24 items-center justify-center rounded-lg bg-primary-50 text-3xl text-primary-600">
                    {profile.name?.charAt(0) ?? "?"}
                  </div>
                )}
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-3">
                    <h2 className="text-lg font-semibold text-text-primary">
                      {profile.name ?? "—"}
                    </h2>
                    <StatusBadge status={profile.status} />
                  </div>
                  <p className="text-sm text-text-secondary">
                    {profile.specialization ?? "—"}
                  </p>
                  <p className="mt-1 text-sm text-text-muted">
                    {profile.bmdcRegNo
                      ? `BMDC ${profile.bmdcRegNo}`
                      : "BMDC number not set"}
                  </p>
                </div>
              </div>
            </CardBody>
          </Card>

          <ProfileForm profile={profile} />
        </>
      )}
    </AppShell>
  );
}