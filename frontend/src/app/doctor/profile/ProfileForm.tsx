"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { Card, CardBody } from "@/components/ui/Card";
import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { FormField, Input, Textarea } from "@/components/ui/Form";
import { saveProfile, type ProfileState } from "./actions";
import type { DoctorProfile } from "@/lib/doctor-api";

export function ProfileForm({ profile }: { profile: DoctorProfile }) {
  const [state, formAction, pending] = useActionState<ProfileState, FormData>(
    saveProfile,
    undefined,
  );

  return (
    <Card>
      <CardBody>
        <form action={formAction} className="flex flex-col gap-4">
          <div className="grid grid-cols-2 gap-4">
            <FormField label="Phone" htmlFor="phoneNumber">
              <Input
                id="phoneNumber"
                name="phoneNumber"
                type="tel"
                defaultValue={profile.phoneNumber ?? ""}
              />
            </FormField>
            <FormField label="Consultation fee" htmlFor="consultationFee">
              <Input
                id="consultationFee"
                name="consultationFee"
                defaultValue={profile.consultationFee ?? ""}
              />
            </FormField>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <FormField label="Division" htmlFor="division">
              <Input
                id="division"
                name="division"
                defaultValue={profile.division ?? ""}
              />
            </FormField>
            <FormField label="District" htmlFor="zilla">
              <Input id="zilla" name="zilla" defaultValue={profile.zilla ?? ""} />
            </FormField>
          </div>

          <FormField label="Address" htmlFor="address">
            <Input
              id="address"
              name="address"
              defaultValue={profile.address ?? ""}
            />
          </FormField>

          <div className="grid grid-cols-2 gap-4">
            <FormField label="Hospital" htmlFor="hospitalName">
              <Input
                id="hospitalName"
                name="hospitalName"
                defaultValue={profile.hospitalName ?? ""}
              />
            </FormField>
            <FormField label="Hospital address" htmlFor="hospitalAddress">
              <Input
                id="hospitalAddress"
                name="hospitalAddress"
                defaultValue={profile.hospitalAddress ?? ""}
              />
            </FormField>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <FormField label="Working days" htmlFor="workingDays">
              <Input
                id="workingDays"
                name="workingDays"
                defaultValue={profile.workingDays ?? ""}
              />
            </FormField>
            <FormField label="Working hours" htmlFor="workingHours">
              <Input
                id="workingHours"
                name="workingHours"
                defaultValue={profile.workingHours ?? ""}
              />
            </FormField>
          </div>

          <FormField label="Languages" htmlFor="languages">
            <Input
              id="languages"
              name="languages"
              defaultValue={profile.languages ?? ""}
            />
          </FormField>

          <FormField label="Certifications" htmlFor="certifications">
            <Input
              id="certifications"
              name="certifications"
              defaultValue={profile.certifications ?? ""}
            />
          </FormField>

          <FormField label="Special interests" htmlFor="specialInterests">
            <Input
              id="specialInterests"
              name="specialInterests"
              defaultValue={profile.specialInterests ?? ""}
            />
          </FormField>

          <FormField label="About" htmlFor="description">
            <Textarea
              id="description"
              name="description"
              rows={5}
              defaultValue={profile.description ?? ""}
            />
          </FormField>

          {state ? (
            state.ok ? (
              <SuccessMessage>{state.message}</SuccessMessage>
            ) : (
              <ErrorMessage>{state.message}</ErrorMessage>
            )
          ) : null}

          <Button type="submit" disabled={pending}>
            {pending ? "Saving…" : "Save profile"}
          </Button>
        </form>
      </CardBody>
    </Card>
  );
}