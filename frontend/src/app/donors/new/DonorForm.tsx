"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/ui/Feedback";
import { FormField, Input, Select } from "@/components/ui/Form";
import { BLOOD_GROUPS } from "@/lib/blood-groups";
import { registerDonor, type DonorFormState } from "./actions";

export function DonorForm({ defaultEmail }: { defaultEmail?: string }) {
  const [state, formAction, pending] = useActionState<DonorFormState, FormData>(
    registerDonor,
    undefined,
  );

  return (
    <form action={formAction} className="flex flex-col gap-4">
      <div className="grid grid-cols-2 gap-4">
        <FormField label="Full name" htmlFor="name" required>
          <Input id="name" name="name" required />
        </FormField>

        <FormField
          label="Age"
          htmlFor="age"
          required
          hint="Donors must be 18 or older."
        >
          <Input
            id="age"
            name="age"
            type="number"
            min={18}
            max={100}
            required
          />
        </FormField>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <FormField label="Blood group" htmlFor="bloodGroup" required>
          <Select id="bloodGroup" name="bloodGroup" defaultValue="" required>
            <option value="">Select a group</option>
            {BLOOD_GROUPS.map((group) => (
              <option key={group} value={group}>
                {group}
              </option>
            ))}
          </Select>
        </FormField>

        <FormField label="Gender" htmlFor="gender">
          <Select id="gender" name="gender" defaultValue="">
            <option value="">Prefer not to say</option>
            <option value="M">Male</option>
            <option value="F">Female</option>
            <option value="O">Other</option>
          </Select>
        </FormField>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <FormField label="Contact number" htmlFor="contact" required>
          <Input id="contact" name="contact" type="tel" required />
        </FormField>

        <FormField label="Email" htmlFor="email" hint="Used for match alerts.">
          <Input id="email" name="email" type="email" defaultValue={defaultEmail} readOnly />
        </FormField>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <FormField label="District" htmlFor="district">
          <Input id="district" name="district" />
        </FormField>

        <FormField label="Thana / area" htmlFor="thana">
          <Input id="thana" name="thana" />
        </FormField>
      </div>

      <FormField
        label="Last donated"
        htmlFor="lastDonated"
        hint="There is a 3-month gap between donations."
      >
        <Input id="lastDonated" name="lastDonated" type="date" />
      </FormField>

      {state?.error ? <ErrorMessage>{state.error}</ErrorMessage> : null}

      <Button type="submit" variant="blood" disabled={pending}>
        {pending ? "Registering…" : "Register as a donor"}
      </Button>
    </form>
  );
}