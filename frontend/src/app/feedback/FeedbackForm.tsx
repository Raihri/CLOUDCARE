"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { ErrorMessage } from "@/components/ui/Feedback";
import { FormField, Select, Textarea } from "@/components/ui/Form";
import { submitFeedback, type FeedbackState } from "./actions";

const CATEGORIES = [
  "General",
  "Blood donation",
  "Appointments",
  "Doctors",
  "Website",
  "Other",
];

const RATINGS = [5, 4, 3, 2, 1];

export function FeedbackForm() {
  const [state, formAction, pending] = useActionState<FeedbackState, FormData>(
    submitFeedback,
    undefined,
  );

  return (
    <form action={formAction} className="flex flex-col gap-4">
      <FormField label="How would you rate us?" htmlFor="rating" required>
        <div className="flex items-center gap-2">
          {RATINGS.map((score) => (
            <label
              key={score}
              className="flex cursor-pointer flex-col items-center gap-1"
            >
              <input
                type="radio"
                name="rating"
                value={score}
                id={score === 5 ? "rating" : undefined}
                required
                className="peer sr-only"
              />
              <span
                className={`flex size-10 items-center justify-center rounded-full border text-sm font-medium transition-colors peer-checked:border-primary-600 peer-checked:bg-primary-50 peer-checked:text-primary-700 ${
                  score >= 4
                    ? "border-warning/40 text-warning"
                    : "border-border text-text-muted"
                }`}
              >
                {score}
              </span>
            </label>
          ))}
        </div>
      </FormField>

      <FormField label="Category" htmlFor="category">
        <Select id="category" name="category" defaultValue="General">
          {CATEGORIES.map((c) => (
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </Select>
      </FormField>

      <FormField
        label="Your feedback"
        htmlFor="comments"
        required
        hint="Tell us what worked well and what could be better."
      >
        <Textarea id="comments" name="comments" rows={5} required />
      </FormField>

      {state?.error ? <ErrorMessage>{state.error}</ErrorMessage> : null}

      <Button type="submit" disabled={pending}>
        {pending ? "Sending…" : "Send feedback"}
      </Button>
    </form>
  );
}