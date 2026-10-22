import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody } from "@/components/ui/Card";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { fetchDoctorSlots } from "@/lib/doctor-api";
import { AddSlotForm, SlotList } from "./SlotControls";

export const metadata: Metadata = { title: "Doctor Availability" };

export const instant = false;

export default async function DoctorAvailabilityPage() {
  const user = await requireRole(["DOCTOR"], "/doctor/availability");
  const slots = await fetchDoctorSlots();

  return (
    <AppShell user={user}>
      <PageHeader
        title="Availability"
        description="Define when patients can book you. Overlapping slots are rejected."
      />

      <div className="flex flex-col gap-5">
        <Card>
          <CardBody>
            <h2 className="mb-4 text-base font-semibold text-text-primary">
              Add a slot
            </h2>
            <AddSlotForm />
          </CardBody>
        </Card>

        <SlotList slots={slots} />
      </div>
    </AppShell>
  );
}