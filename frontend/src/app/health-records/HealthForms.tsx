"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { Card, CardBody } from "@/components/ui/Card";
import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { FormField, Input, Select } from "@/components/ui/Form";
import { BLOOD_GROUPS } from "@/lib/blood-groups";
import {
  saveEnvironment,
  saveLifestyle,
  saveMenstrual,
  savePersonal,
  saveVitals,
  type SaveState,
} from "./actions";
import type { HealthRecord } from "./types";

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

function Section({
  title,
  description,
  children,
}: {
  title: string;
  description?: string;
  children: React.ReactNode;
}) {
  return (
    <Card>
      <CardBody>
        <h2 className="text-base font-semibold text-text-primary">{title}</h2>
        {description ? (
          <p className="mt-0.5 text-sm text-text-muted">{description}</p>
        ) : null}
        <div className="mt-4">{children}</div>
      </CardBody>
    </Card>
  );
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

export function HealthForms({ record }: { record: HealthRecord }) {
  const isFemale = record.gender === "F";

  const [personal, personalAction, personalPending] = useActionState<
    SaveState,
    FormData
  >(savePersonal, undefined);
  const [vitals, vitalsAction, vitalsPending] = useActionState<SaveState, FormData>(
    saveVitals,
    undefined,
  );
  const [lifestyle, lifestyleAction, lifestylePending] = useActionState<
    SaveState,
    FormData
  >(saveLifestyle, undefined);
  const [menstrual, menstrualAction, menstrualPending] = useActionState<
    SaveState,
    FormData
  >(saveMenstrual, undefined);
  const [environment, environmentAction, environmentPending] = useActionState<
    SaveState,
    FormData
  >(saveEnvironment, undefined);

  return (
    <div className="flex flex-col gap-5">
      {/* ---------------- Personal ---------------- */}
      <Section
        title="Personal information"
        description="The basics your doctors see when they open your record."
      >
        <form action={personalAction} className="flex flex-col gap-4">
          <div className="grid grid-cols-3 gap-4">
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
              <Select id="gender" name="gender" defaultValue={record.gender ?? ""}>
                <option value="">Not set</option>
                <option value="M">Male</option>
                <option value="F">Female</option>
                <option value="O">Other</option>
                <option value="U">Prefer not to say</option>
              </Select>
            </FormField>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <FormField label="Date of birth" htmlFor="dateOfBirth">
              <Input
                id="dateOfBirth"
                name="dateOfBirth"
                type="date"
                defaultValue={str(record.dateOfBirth)}
              />
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

          <FormField label="Address" htmlFor="address">
            <Input
              id="address"
              name="address"
              defaultValue={str(record.address)}
            />
          </FormField>

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

          <div className="grid grid-cols-3 gap-4">
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
            <FormField label="Last donated" htmlFor="lastDonated">
              <Input
                id="lastDonated"
                name="lastDonated"
                type="date"
                defaultValue={str(record.lastDonated)}
              />
            </FormField>
          </div>

          <Button type="submit" disabled={personalPending}>
            {personalPending ? "Saving…" : "Save personal information"}
          </Button>
          <Banner state={personal} />
        </form>
      </Section>

      {/* ---------------- Vitals ---------------- */}
      <Section
        title="Vital signs"
        description="Latest readings. Leave a field blank to keep its current value."
      >
        <form action={vitalsAction} className="flex flex-col gap-4">
          <div className="grid grid-cols-2 gap-4">
            <FormField label="Systolic BP (mmHg)" htmlFor="systolicBP">
              <Input
                id="systolicBP"
                name="systolicBP"
                type="number"
                defaultValue={num(record.systolicBP)}
              />
            </FormField>
            <FormField label="Diastolic BP (mmHg)" htmlFor="diastolicBP">
              <Input
                id="diastolicBP"
                name="diastolicBP"
                type="number"
                defaultValue={num(record.diastolicBP)}
              />
            </FormField>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <FormField label="Heart rate (bpm)" htmlFor="heartRate">
              <Input
                id="heartRate"
                name="heartRate"
                type="number"
                defaultValue={num(record.heartRate)}
              />
            </FormField>
            <FormField label="Oxygen saturation (%)" htmlFor="oxygenSaturation">
              <Input
                id="oxygenSaturation"
                name="oxygenSaturation"
                type="number"
                step="0.1"
                defaultValue={num(record.oxygenSaturation)}
              />
            </FormField>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <FormField label="Respiratory rate" htmlFor="respiratoryRate">
              <Input
                id="respiratoryRate"
                name="respiratoryRate"
                type="number"
                step="0.1"
                defaultValue={num(record.respiratoryRate)}
              />
            </FormField>
            <FormField label="Temperature (°C)" htmlFor="bodyTemperature">
              <Input
                id="bodyTemperature"
                name="bodyTemperature"
                type="number"
                step="0.1"
                defaultValue={num(record.bodyTemperature)}
              />
            </FormField>
            <FormField label="Blood sugar (mg/dL)" htmlFor="bloodSugarLevel">
              <Input
                id="bloodSugarLevel"
                name="bloodSugarLevel"
                type="number"
                step="0.1"
                defaultValue={num(record.bloodSugarLevel)}
              />
            </FormField>
          </div>

          <Button type="submit" disabled={vitalsPending}>
            {vitalsPending ? "Saving…" : "Save vital signs"}
          </Button>
          <Banner state={vitals} />
        </form>
      </Section>

      {/* ---------------- Lifestyle ---------------- */}
      <Section
        title="Lifestyle"
        description="Habits that affect your risk profile."
      >
        <form action={lifestyleAction} className="flex flex-col gap-4">
          <div className="grid grid-cols-3 gap-4">
            <FormField label="Sleep (hours/night)" htmlFor="sleepHours">
              <Input
                id="sleepHours"
                name="sleepHours"
                type="number"
                step="0.5"
                defaultValue={num(record.sleepHours)}
              />
            </FormField>
            <FormField label="Water (litres/day)" htmlFor="waterIntake">
              <Input
                id="waterIntake"
                name="waterIntake"
                type="number"
                step="0.5"
                defaultValue={num(record.waterIntake)}
              />
            </FormField>
            <FormField
              label="Physical activity"
              htmlFor="physicalActivity"
              hint="Days per week"
            >
              <Input
                id="physicalActivity"
                name="physicalActivity"
                type="number"
                min={0}
                max={7}
                defaultValue={num(record.physicalActivity)}
              />
            </FormField>
          </div>

          <div className="flex gap-6">
            <label className="flex items-center gap-2 text-sm text-text-secondary">
              <input
                type="checkbox"
                name="smoker"
                value="true"
                defaultChecked={record.smoker}
                className="h-4 w-4 accent-blood"
              />
              I smoke
            </label>
            <label className="flex items-center gap-2 text-sm text-text-secondary">
              <input
                type="checkbox"
                name="alcoholConsumer"
                value="true"
                defaultChecked={record.alcoholConsumer}
                className="h-4 w-4 accent-blood"
              />
              I drink alcohol
            </label>
          </div>

          <Button type="submit" disabled={lifestylePending}>
            {lifestylePending ? "Saving…" : "Save lifestyle"}
          </Button>
          <Banner state={lifestyle} />
        </form>
      </Section>

      {/* ---------------- Menstrual ---------------- */}
      <Section
        title="Menstrual cycle"
        description={
          isFemale
            ? "Only applies to female patients."
            : "Hidden — this section only applies to female patients."
        }
      >
        {isFemale ? (
          <form action={menstrualAction} className="flex flex-col gap-4">
            <div className="flex flex-wrap gap-6">
              <label className="flex items-center gap-2 text-sm text-text-secondary">
                <input
                  type="checkbox"
                  name="pregnant"
                  value="true"
                  defaultChecked={Boolean(record.pregnant)}
                  className="h-4 w-4 accent-azure"
                />
                Currently pregnant
              </label>
              <label className="flex items-center gap-2 text-sm text-text-secondary">
                <input
                  type="checkbox"
                  name="breastFeeding"
                  value="true"
                  defaultChecked={Boolean(record.breastFeeding)}
                  className="h-4 w-4 accent-azure"
                />
                Breastfeeding
              </label>
              <label className="flex items-center gap-2 text-sm text-text-secondary">
                <input
                  type="checkbox"
                  name="menstruationRegular"
                  value="true"
                  defaultChecked={Boolean(record.menstruationRegular)}
                  className="h-4 w-4 accent-azure"
                />
                Cycle is regular
              </label>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <FormField label="Pregnancy week" htmlFor="pregnancyWeek">
                <Input
                  id="pregnancyWeek"
                  name="pregnancyWeek"
                  type="number"
                  min={0}
                  max={42}
                  defaultValue={num(record.pregnancyWeek)}
                />
              </FormField>
              <FormField label="Last menstrual date" htmlFor="lastMenstrualDate">
                <Input
                  id="lastMenstrualDate"
                  name="lastMenstrualDate"
                  type="date"
                  defaultValue={str(record.lastMenstrualDate)}
                />
              </FormField>
            </div>

            <Button type="submit" disabled={menstrualPending}>
              {menstrualPending ? "Saving…" : "Save cycle information"}
            </Button>
            <Banner state={menstrual} />
          </form>
        ) : null}
      </Section>

      {/* ---------------- Environment & family ---------------- */}
      <Section
        title="Environment & family"
        description="Your home situation and support system."
      >
        <form action={environmentAction} className="flex flex-col gap-4">
          <FormField label="Parental care" htmlFor="parentalCareState">
            <Input
              id="parentalCareState"
              name="parentalCareState"
              defaultValue={str(record.parentalCareState)}
            />
          </FormField>
          <FormField label="Home environment" htmlFor="homeEnvironment">
            <Input
              id="homeEnvironment"
              name="homeEnvironment"
              defaultValue={str(record.homeEnvironment)}
            />
          </FormField>

          <div className="flex gap-6">
            <label className="flex items-center gap-2 text-sm text-text-secondary">
              <input
                type="checkbox"
                name="haveBeenBullied"
                value="true"
                defaultChecked={Boolean(record.haveBeenBullied)}
                className="h-4 w-4 accent-azure"
              />
              I have been bullied
            </label>
            <label className="flex items-center gap-2 text-sm text-text-secondary">
              <input
                type="checkbox"
                name="feelingLeftOut"
                value="true"
                defaultChecked={Boolean(record.feelingLeftOut)}
                className="h-4 w-4 accent-azure"
              />
              I often feel left out
            </label>
          </div>

          <Button type="submit" disabled={environmentPending}>
            {environmentPending ? "Saving…" : "Save environment & family"}
          </Button>
          <Banner state={environment} />
        </form>
      </Section>
    </div>
  );
}