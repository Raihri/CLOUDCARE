"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/ui/Feedback";
import { FormField, Input, Select, Textarea } from "@/components/ui/Form";
import { BLOOD_GROUPS } from "@/lib/blood-groups";
import { createBloodRequest, type RequestFormState } from "./actions";

export function RequestForm({ defaultEmail }: { defaultEmail?: string }) {
  const [state, formAction, pending] = useActionState<RequestFormState, FormData>(
    createBloodRequest,
    undefined,
  );

  return (
    <form action={formAction} className="flex flex-col gap-4">
      <div className="grid grid-cols-2 gap-4">
        <FormField label="Your name" htmlFor="name" required>
          <Input id="name" name="name" required />
        </FormField>

        <FormField label="Contact email" htmlFor="email" required>
          <Input
            id="email"
            name="email"
            type="email"
            defaultValue={defaultEmail}
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

        <FormField label="Units needed" htmlFor="units" required>
          <Input
            id="units"
            name="units"
            type="number"
            min={1}
            max={20}
            defaultValue={1}
            required
          />
        </FormField>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <FormField
          label="District"
          htmlFor="district"
          hint="Helps us match donors nearby."
        >
          <Input id="district" name="district" />
        </FormField>

        <FormField label="Thana / area" htmlFor="thana">
          <Input id="thana" name="thana" />
        </FormField>
      </div>

      <FormField label="Needed by" htmlFor="requiredDate">
        <Input id="requiredDate" name="requiredDate" type="date" />
      </FormField>

      <FormField label="Medical reason" htmlFor="medicalReason">
        <Textarea
          id="medicalReason"
          name="medicalReason"
          placeholder="A short note helps donors understand the urgency."
        />
      </FormField>

      {state?.error ? <ErrorMessage>{state.error}</ErrorMessage> : null}

      <Button type="submit" variant="blood" disabled={pending}>
        {pending ? "Posting…" : "Post blood request"}
      </Button>
    </form>
  );
}