import type { Metadata } from "next";

import type { HealthRecord } from "@/app/health-records/types";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { EmptyState, StatCard } from "@/components/ui/Feedback";
import { PageHeader } from "@/components/ui/PageHeader";
import { springJson } from "@/lib/api";
import { requireRole } from "@/lib/auth";

export const metadata: Metadata = { title: "Health Metrics" };

export const instant = false;

/* ---------------------------------------------------------------- helpers */

/** "Not recorded" rather than an empty cell, so a gap is visibly a gap. */
function val(value: number | string | null | undefined, unit = ""): string {
  if (value === null || value === undefined || value === "") return "Not recorded";
  return `${value}${unit}`;
}

function yesNo(value: boolean | null | undefined): string {
  if (value === null || value === undefined) return "Not recorded";
  return value ? "Yes" : "No";
}

/** The backend stores gender as single letters; a profile should spell it out. */
const GENDER_LABEL: Record<string, string> = {
  M: "Male",
  F: "Female",
  O: "Other",
  U: "Prefer not to say",
};

function genderLabel(value: string | null | undefined): string {
  if (!value) return "Not set";
  return GENDER_LABEL[value] ?? value;
}

/** BMI is only worth flagging when it is actually recorded and in range. */
function bmiHint(value: number | null): string | undefined {
  if (value == null) return undefined;
  const n = Number(value);
  if (n < 18.5) return "Underweight";
  if (n > 24.9) return "Overweight";
  return "Healthy range";
}

/** A label/value pair on a tinted row. */
function Reading({
  label,
  value,
  highlight = false,
}: {
  label: string;
  value: string;
  highlight?: boolean;
}) {
  return (
    <div
      className={[
        "flex items-baseline justify-between gap-4 rounded-lg px-3 py-2",
        highlight ? "bg-primary-50" : "bg-surface-sunken",
      ].join(" ")}
    >
      <span className="text-sm text-text-secondary">{label}</span>
      <span
        className={[
          "text-sm font-semibold tabular-nums",
          value === "Not recorded" ? "text-text-muted" : "text-text-primary",
        ].join(" ")}
      >
        {value}
      </span>
    </div>
  );
}

/** A yes/no habit chip. */
function Habit({ on, label }: { on: boolean; label: string }) {
  return (
    <span
      className={[
        "inline-flex items-center gap-1.5 rounded-full px-3 py-1.5 text-xs font-medium",
        on
          ? "bg-danger-soft text-danger"
          : "bg-success-soft text-success",
      ].join(" ")}
    >
      <i
        className={`bx ${on ? "bx-x-circle" : "bx-check-circle"} text-sm`}
        aria-hidden
      />
      {label}
    </span>
  );
}

/* ----------------------------------------------------------------- page */

export default async function HealthMetricsPage() {
  const user = await requireRole(["PATIENT"], "/health-metrics");

  let record: HealthRecord | null = null;
  let error: string | null = null;
  try {
    record = await springJson<HealthRecord>("/patient/api/health");
  } catch {
    error =
      "We couldn't load your health metrics. Please refresh and try again.";
  }

  const bmi =
    record?.bmi != null
      ? Number(record.bmi).toFixed(1)
      : "Not recorded";

  return (
    <AppShell user={user}>
      <PageHeader
        title="Health Metrics"
        description="The numbers your doctors look at — vitals, body, lifestyle and wellbeing."
      />

      {error || !record ? (
        <Card>
          <CardBody>
            <EmptyState
              icon={<i className="bx bxs-heart" aria-hidden />}
              title="No metrics yet"
              description={error ?? "Fill in your health record to see your metrics here."}
            />
          </CardBody>
        </Card>
      ) : (
        <>
          {/* ---- At-a-glance ---- */}
          <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
            <StatCard
              label="Blood group"
              value={record.bloodGroup ?? "—"}
              icon={<i className="bx bxs-droplet" aria-hidden />}
            />
            <StatCard
              label="BMI"
              value={bmi}
              hint={bmiHint(record.bmi)}
              icon={<i className="bx bxs-chart" aria-hidden />}
            />
            <StatCard
              label="Blood pressure"
              value={
                record.systolicBP != null || record.diastolicBP != null
                  ? `${val(record.systolicBP)}/${val(record.diastolicBP)}`
                  : "—"
              }
              hint="mmHg"
              icon={<i className="bx bxs-heart" aria-hidden />}
            />
            <StatCard
              label="Last donated"
              value={record.lastDonated ?? "—"}
              icon={<i className="bx bxs-droplet-half" aria-hidden />}
            />
          </div>

          <div className="grid gap-4 lg:grid-cols-2">
            {/* ---- Vitals ---- */}
            <Card>
              <CardHeader
                title="Vitals"
                description="Latest recorded readings."
              />
              <CardBody className="flex flex-col gap-2">
                <Reading label="Heart rate" value={val(record.heartRate, " bpm")} highlight />
                <Reading
                  label="Blood pressure"
                  value={
                    record.systolicBP != null || record.diastolicBP != null
                      ? `${val(record.systolicBP)}/${val(record.diastolicBP)} mmHg`
                      : "Not recorded"
                  }
                  highlight
                />
                <Reading label="Oxygen saturation" value={val(record.oxygenSaturation, " %")} />
                <Reading label="Respiratory rate" value={val(record.respiratoryRate, " /min")} />
                <Reading label="Body temperature" value={val(record.bodyTemperature, " °C")} />
                <Reading label="Blood sugar" value={val(record.bloodSugarLevel, " mg/dL")} />
              </CardBody>
            </Card>

            {/* ---- Body ---- */}
            <Card>
              <CardHeader
                title="Body"
                description="Measurements taken at your last visit."
              />
              <CardBody className="flex flex-col gap-2">
                <Reading label="Weight" value={val(record.weight, " kg")} />
                <Reading label="Height" value={val(record.height, " cm")} />
                <Reading label="BMI" value={bmi} />
                <Reading label="Blood group" value={val(record.bloodGroup)} />
                <Reading label="Date of birth" value={val(record.dateOfBirth)} />
                <Reading label="Gender" value={genderLabel(record.gender)} />
              </CardBody>
            </Card>

            {/* ---- Lifestyle ---- */}
            <Card>
              <CardHeader
                title="Lifestyle"
                description="Daily habits that affect your risk profile."
              />
              <CardBody className="flex flex-col gap-4">
                <div className="flex flex-col gap-2">
                  <Reading label="Sleep" value={val(record.sleepHours, " hrs/night")} />
                  <Reading label="Water intake" value={val(record.waterIntake, " L/day")} />
                  <Reading
                    label="Physical activity"
                    value={val(record.physicalActivity, " days/week")}
                  />
                </div>
                <div className="flex flex-wrap gap-2">
                  <Habit on={record.smoker} label={record.smoker ? "Smoker" : "Non-smoker"} />
                  <Habit
                    on={record.alcoholConsumer}
                    label={record.alcoholConsumer ? "Drinks alcohol" : "No alcohol"}
                  />
                </div>
              </CardBody>
            </Card>

            {/* ---- Wellbeing ---- */}
            <Card>
              <CardHeader
                title="Wellbeing"
                description="Home situation and how you've been feeling."
              />
              <CardBody className="flex flex-col gap-2">
                <Reading label="Parental care" value={val(record.parentalCareState)} />
                <Reading label="Home environment" value={val(record.homeEnvironment)} />
                <Reading
                  label="Have been bullied"
                  value={yesNo(record.haveBeenBullied)}
                />
                <Reading
                  label="Often feel left out"
                  value={yesNo(record.feelingLeftOut)}
                />
                {record.gender === "F" ? (
                  <>
                    <Reading label="Cycle regular" value={yesNo(record.menstruationRegular)} />
                    <Reading label="Last menstrual date" value={val(record.lastMenstrualDate)} />
                  </>
                ) : null}
              </CardBody>
            </Card>
          </div>
        </>
      )}
    </AppShell>
  );
}