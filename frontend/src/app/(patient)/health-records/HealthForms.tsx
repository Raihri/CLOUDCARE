"use client";

import { useActionState, useEffect, useRef, useState } from "react";

import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { FormField, Input, Select } from "@/components/ui/Form";
import { BLOOD_GROUPS } from "@/lib/blood-groups";
import { savePersonal, savePhoto, type SaveState } from "./actions";
import type { HealthRecord } from "@/lib/health-record";

/** Number inputs need a string; null must become "" so an untouched field is
 *  left out of the request entirely — the backend reads that as "leave this
 *  value alone" rather than "set it to zero". */
function num(value: number | string | null | undefined): string {
  const s = value == null ? "" : String(value).trim();
  return s === "" ? "" : s;
}

function str(value: string | null | undefined): string {
  return value ?? "";
}

function Banner({ state }: { state: SaveState }) {
  if (!state) return null;
  return (
    <div className="mt-3">
      {state.ok ? <SuccessMessage>{state.message}</SuccessMessage> : null}
      {!state.ok ? <ErrorMessage>{state.message}</ErrorMessage> : null}
    </div>
  );
}

/* ----------------------------------------------------------- photo picker */

const MAX_BYTES = 5 * 1024 * 1024;

function initials(name: string | null, email: string | null): string {
  const source = (name ?? email ?? "").trim();
  if (!source) return "?";
  const parts = source.split(/[\s@._-]+/).filter(Boolean);
  if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
  return source.slice(0, 2).toUpperCase();
}

/**
 * Avatar uploader. The picked file is previewed with an object URL, which must
 * be revoked on the next pick or unmount or it pins the whole blob in memory
 * for the life of the tab.
 */
function PhotoPicker({ record }: { record: HealthRecord }) {
  const [state, action, pending] = useActionState<SaveState, FormData>(
    savePhoto,
    undefined,
  );
  const [preview, setPreview] = useState<string | null>(null);
  const [localError, setLocalError] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!preview) return;
    return () => URL.revokeObjectURL(preview);
  }, [preview]);

  function onPick(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      setLocalError("That file isn't an image.");
      e.target.value = "";
      return;
    }
    if (file.size > MAX_BYTES) {
      setLocalError("Photo must be smaller than 5 MB.");
      e.target.value = "";
      return;
    }

    setLocalError(null);
    setPreview(URL.createObjectURL(file));
  }

  const shown = preview ?? record.photoUrl;

  // NOTE: this is deliberately NOT a <form>. It renders its own server-action
  // submit, but the caller wraps everything in one outer <form> for the
  // personal-info fields — and nested forms are invalid HTML, which breaks
  // the browser's form parsing and React's hydration. The button below
  // references `action` by function form, which React turns into an implicit
  // submission without needing a <form> wrapper.
  return (
    <Card className="overflow-hidden">
      {/* Avatar sits on the left, full-bleed of the card's height, so the eye
          lands on the thing the section is actually about. */}
      <div className="flex flex-col items-center gap-6 p-6 sm:flex-row sm:items-center">
        <div className="shrink-0">
          <div className="relative">
            {shown ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img
                src={shown}
                alt={`${record.name ?? "Patient"}'s photo`}
                className="size-32 rounded-2xl border border-border object-cover"
              />
            ) : (
              <div
                className="flex size-32 items-center justify-center rounded-2xl bg-primary-50 text-3xl font-bold text-primary-800"
                aria-label="Patient photo placeholder"
              >
                {initials(record.name, record.email)}
              </div>
            )}

            {/* Overlay camera affordance. The button sits over the image
                rather than beside it so "change photo" is visibly an act on
                the photo itself. */}
            {preview ? (
              <button
                type="button"
                onClick={() => inputRef.current?.click()}
                className="absolute inset-0 flex cursor-pointer items-center justify-center rounded-2xl bg-neutral-900/55 text-xs font-semibold text-white opacity-0 transition-opacity hover:opacity-100 focus-visible:opacity-100"
              >
                Replace
              </button>
            ) : null}
          </div>
        </div>

        <div className="min-w-0 flex-1 text-center sm:text-left">
          <h2 className="text-base font-semibold text-text-primary">
            Profile photo
          </h2>
          <p className="mt-1 text-sm text-text-muted">
            A clear headshot helps doctors recognise you at an appointment.
          </p>

          <div className="mt-4">
            <div className="flex flex-wrap items-center justify-center gap-3 sm:justify-start">
              {/* Label-as-button: gives us the platform's native file picker
                  and native "Photos"/"Choose Files" affordance for free, which
                  a styled div would have to reimplement badly. */}
              <label className="inline-flex h-9 cursor-pointer items-center gap-2 rounded-md border border-primary-600 bg-surface px-3.5 text-sm font-medium text-primary-900 transition-colors hover:bg-primary-50">
                <i className="bx bxs-camera" aria-hidden />
                {preview ? "Choose another" : "Choose photo"}
                <input
                  ref={inputRef}
                  type="file"
                  name="file"
                  accept="image/*"
                  className="sr-only"
                  onChange={onPick}
                />
              </label>

              {/* Disabled until a file is picked. `secondary` rather than
                  `primary` because half-opacity navy on white still reads as
                  a solid, clickable button — this stays unmistakably inert. */}
              <Button
                type={action}
                size="sm"
                variant="secondary"
                className="h-9"
                disabled={pending || !preview}
              >
                {pending ? "Uploading…" : "Upload"}
              </Button>
            </div>

            <p className="mt-2.5 text-xs text-text-muted">
              JPG, PNG or WebP · up to 5 MB
            </p>

            {localError ? (
              <div className="mt-2">
                <ErrorMessage>{localError}</ErrorMessage>
              </div>
            ) : null}
            <Banner state={state} />
          </div>
        </div>
      </div>
    </Card>
  );
}

/* --------------------------------------------------------------- sections */

/**
 * A titled group of related fields.
 *
 * <fieldset> rather than a plain <div>: it gives the group a real accessible
 * name, so a screen reader announces "Personal details, group" when focus
 * enters it instead of reading a wall of unlabelled inputs.
 */
function Group({
  title,
  description,
  icon,
  children,
  className = "",
}: {
  title: string;
  description: string;
  icon: string;
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <fieldset className={`min-w-0 ${className}`}>
      <legend className="sr-only">{title}</legend>
      <div className="flex items-start gap-3">
        <span
          className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-highlight"
          aria-hidden
        >
          <i className={`bx ${icon} text-lg`} />
        </span>
        <div className="min-w-0">
          <h3 className="text-sm font-semibold text-text-primary">{title}</h3>
          <p className="mt-0.5 text-xs text-text-muted">{description}</p>
        </div>
      </div>
      <div className="mt-4 grid gap-4">{children}</div>
    </fieldset>
  );
}

/* ------------------------------------------------------------------ forms */

export function HealthForms({ record }: { record: HealthRecord }) {
  const [personal, personalAction, personalPending] = useActionState<
    SaveState,
    FormData
  >(savePersonal, undefined);

  // Dirty tracking, done by comparing the form's live serialized value against
  // a snapshot taken at mount.
  //
  // This deliberately does NOT hang an onChange on each of the 15 controls.
  // Per-field handlers need React to attach a listener to every input, and
  // under this app's streaming/Suspense setup those listeners were silently
  // not being attached — the handler was present in the fiber props but never
  // fired, so "unsaved changes" never appeared. Reading the DOM directly is
  // immune to that, and one delegated listener beats fifteen props.
  const formRef = useRef<HTMLFormElement>(null);
  const baseline = useRef("");
  const [dirty, setDirty] = useState(false);

  /** Every named control's value, joined — the form's "state" in one string. */
  function snapshot(): string {
    const form = formRef.current;
    if (!form) return "";
    return Array.from(form.elements)
      .map((el) => {
        const field = el as HTMLInputElement;
        if (!field.name || field.disabled) return "";
        return `${field.name}=${field.value}`;
      })
      .join("&");
  }

  // Capture the starting point once mounted, and again after every successful
  // save — at which point what's on screen IS what's stored.
  useEffect(() => {
    if (!personal?.ok && baseline.current === "") {
      baseline.current = snapshot();
    } else if (personal?.ok) {
      baseline.current = "";
    }
  }, [personal]);

  // Recompute on any input/change that bubbles to the form, and re-baseline
  // right after a successful save.
  useEffect(() => {
    const form = formRef.current;
    if (!form) return;

    if (baseline.current === "") baseline.current = snapshot();

    const check = () => {
      const base = baseline.current === "" ? (baseline.current = snapshot()) : baseline.current;
      setDirty(snapshot() !== base);
    };

    // `input` covers typing in every text control AND selects/date pickers;
    // `change` is the belt-and-braces for browsers that suppress `input`.
    form.addEventListener("input", check);
    form.addEventListener("change", check);

    return () => {
      form.removeEventListener("input", check);
      form.removeEventListener("change", check);
    };
  }, [personal]);

  return (
    <div className="flex flex-col gap-5">
      {/* Photo is deliberately OUTSIDE the personal-info <form>. A file input
          nested inside it would ride along with every personal-info save,
          re-uploading the image on unrelated edits. */}
      <PhotoPicker record={record} />

      <form ref={formRef} action={personalAction}>
        <Card>
        <div className="border-b border-border px-5 py-4 sm:px-6">
          <h2 className="text-base font-semibold text-text-primary">
            Personal information
          </h2>
          <p className="mt-0.5 text-sm text-text-muted">
            The basics your doctors see when they open your record.
          </p>
        </div>

        <div className="grid gap-8 px-5 py-5 sm:px-6 sm:py-6 lg:grid-cols-2">
          {/* ---- Identity ---- */}
          <Group
            title="Identity"
            description="How you're identified on your records."
            icon="bxs-user"
          >
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Blood group" htmlFor="bloodGroup">
                <Select
                  id="bloodGroup"
                  name="bloodGroup"
                  defaultValue={record.bloodGroup ?? ""}
                >
                  <option value="">Not set</option>
                  {BLOOD_GROUPS.map((g) => (
                    <option key={g} value={g}>
                      {g}
                    </option>
                  ))}
                </Select>
              </FormField>
              <FormField label="National ID" htmlFor="nationalId">
                <Input
                  id="nationalId"
                  name="nationalId"
                  defaultValue={str(record.nationalId)}
                />
              </FormField>
            </div>

            <div className="grid grid-cols-3 gap-4">
              <FormField label="Age" htmlFor="age">
                <Input
                  id="age"
                  name="age"
                  type="number"
                  min={0}
                  max={130}
                  defaultValue={num(record.age)}
                />
              </FormField>
              <FormField label="Gender" htmlFor="gender">
                <Select
                  id="gender"
                  name="gender"
                  defaultValue={record.gender ?? ""}
                >
                  <option value="">Not set</option>
                  <option value="M">Male</option>
                  <option value="F">Female</option>
                  <option value="O">Other</option>
                  <option value="U">Prefer not to say</option>
                </Select>
              </FormField>
              <FormField label="Date of birth" htmlFor="dateOfBirth">
                <Input
                  id="dateOfBirth"
                  name="dateOfBirth"
                  type="date"
                  defaultValue={str(record.dateOfBirth)}
                />
              </FormField>
            </div>
          </Group>

          {/* ---- Body measurements ---- */}
          <Group
            title="Body measurements"
            description="Used to calculate your BMI on save."
            icon="bxs-chart"
          >
            <div className="grid grid-cols-3 gap-4">
              <FormField label="Weight (kg)" htmlFor="weight">
                <Input
                  id="weight"
                  name="weight"
                  type="number"
                  step="0.1"
                  defaultValue={num(record.weight)}
                />
              </FormField>
              <FormField label="Height (cm)" htmlFor="height">
                <Input
                  id="height"
                  name="height"
                  type="number"
                  step="0.1"
                  defaultValue={num(record.height)}
                />
              </FormField>
              <FormField
                label="BMI"
                htmlFor="bmi"
                hint={
                  record.bmi != null ? record.bmi.toFixed(1) : "Calculated on save"
                }
              >
                <Input id="bmi" disabled defaultValue="" readOnly />
              </FormField>
            </div>
          </Group>

          {/* ---- Address ---- */}
          <Group
            title="Address"
            description="Where we can reach you."
            icon="bx-map-alt"
          >
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Division" htmlFor="division">
                <Input
                  id="division"
                  name="division"
                  defaultValue={str(record.division)}
                />
              </FormField>
              <FormField label="District (zilla)" htmlFor="zilla">
                <Input
                  id="zilla"
                  name="zilla"
                  defaultValue={str(record.zilla)}
                />
              </FormField>
            </div>
            <FormField label="Street address" htmlFor="address">
              <Input
                id="address"
                name="address"
                defaultValue={str(record.address)}
              />
            </FormField>
          </Group>

          {/* ---- Family & emergency ---- */}
          <Group
            title="Family & emergency"
            description="Who to contact, and who to contact you."
            icon="bxs-group"
          >
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Father's name" htmlFor="fatherName">
                <Input
                  id="fatherName"
                  name="fatherName"
                  defaultValue={str(record.fatherName)}
                />
              </FormField>
              <FormField label="Mother's name" htmlFor="motherName">
                <Input
                  id="motherName"
                  name="motherName"
                  defaultValue={str(record.motherName)}
                />
              </FormField>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Emergency contact" htmlFor="emergencyContact">
                <Input
                  id="emergencyContact"
                  name="emergencyContact"
                  defaultValue={str(record.emergencyContact)}
                />
              </FormField>
              <FormField label="Marital status" htmlFor="maritalStatus">
                <Input
                  id="maritalStatus"
                  name="maritalStatus"
                  defaultValue={str(record.maritalStatus)}
                />
              </FormField>
            </div>
            <FormField label="Last donated" htmlFor="lastDonated">
              <Input
                id="lastDonated"
                name="lastDonated"
                type="date"
                defaultValue={str(record.lastDonated)}
              />
            </FormField>
          </Group>
        </div>

        {/* ---- Sticky save bar ----
            The form is 15 fields tall, so a button at the very bottom is easy
            to scroll past. This sticks to the bottom of the viewport while the
            form is in view, and only appears once something has actually
            changed — otherwise it would be permanent furniture. */}
        <div
          className={[
            "sticky bottom-0 z-10 flex items-center justify-between gap-4 rounded-b-lg border-t border-border bg-surface/95 px-5 py-3 backdrop-blur transition-opacity sm:px-6",
            dirty ? "opacity-100" : "pointer-events-none opacity-0",
          ].join(" ")}
          aria-hidden={!dirty}
        >
          <p className="text-xs text-text-muted">
            You have unsaved changes to your personal information.
          </p>
          <Button type="submit" size="sm" disabled={personalPending}>
            {personalPending ? "Saving…" : "Save changes"}
          </Button>
        </div>

        <div className="px-5 pb-5 sm:px-6">
          <Banner state={personal} />
        </div>
        </Card>
      </form>
    </div>
  );
}