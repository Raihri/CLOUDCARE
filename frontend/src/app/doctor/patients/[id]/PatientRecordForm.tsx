"use client";

import { useActionState } from "react";

import { Button } from "@/components/ui/Button";
import { Card, CardBody, CardHeader } from "@/components/ui/Card";
import { ErrorMessage, SuccessMessage } from "@/components/ui/Feedback";
import { FormField, Input, Select, Textarea } from "@/components/ui/Form";
import type { DoctorPatientRecord } from "@/lib/doctor-api";
import { updatePatient, type PatientUpdateState } from "./actions";

const value = (input: string | number | null | undefined) =>
  input === null || input === undefined ? "" : String(input);

const csv = (items: string[]) => items.join(", ");

export function PatientRecordForm({
  patient,
}: {
  patient: DoctorPatientRecord;
}) {
  const [state, formAction, pending] = useActionState<
    PatientUpdateState,
    FormData
  >(updatePatient, undefined);

  return (
    <form action={formAction} className="flex flex-col gap-5">
      <input type="hidden" name="patientId" value={patient.id} />

      <Card>
        <CardHeader
          title="Personal information"
          description="Identity and contact details shared with the patient."
        />
        <CardBody>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <FormField label="Full name" htmlFor="name">
              <Input
                id="name"
                name="name"
                defaultValue={value(patient.name)}
                autoComplete="name"
              />
            </FormField>
            <FormField label="Age" htmlFor="age">
              <Input
                id="age"
                name="age"
                type="number"
                min={0}
                max={130}
                defaultValue={value(patient.age)}
              />
            </FormField>
            <FormField label="Gender" htmlFor="gender">
              <Select
                id="gender"
                name="gender"
                defaultValue={value(patient.gender)}
              >
                <option value="U">Unknown</option>
                <option value="F">Female</option>
                <option value="M">Male</option>
                <option value="O">Other</option>
              </Select>
            </FormField>
            <FormField label="Blood group" htmlFor="bloodGroup">
              <Input
                id="bloodGroup"
                name="bloodGroup"
                defaultValue={value(patient.bloodGroup)}
                placeholder="e.g. O+"
              />
            </FormField>
            <FormField label="Date of birth" htmlFor="dateOfBirth">
              <Input
                id="dateOfBirth"
                name="dateOfBirth"
                type="date"
                defaultValue={value(patient.dateOfBirth)}
              />
            </FormField>
            <FormField label="Marital status" htmlFor="maritalStatus">
              <Input
                id="maritalStatus"
                name="maritalStatus"
                defaultValue={value(patient.maritalStatus)}
              />
            </FormField>
            <FormField label="National ID" htmlFor="nationalId">
              <Input
                id="nationalId"
                name="nationalId"
                defaultValue={value(patient.nationalId)}
              />
            </FormField>
            <FormField label="Emergency contact" htmlFor="emergencyContact">
              <Input
                id="emergencyContact"
                name="emergencyContact"
                defaultValue={value(patient.emergencyContact)}
              />
            </FormField>
            <FormField label="Weight (kg)" htmlFor="weight">
              <Input
                id="weight"
                name="weight"
                type="number"
                step="0.1"
                min={0}
                defaultValue={value(patient.weight)}
              />
            </FormField>
            <FormField label="Height (cm)" htmlFor="height">
              <Input
                id="height"
                name="height"
                type="number"
                step="0.1"
                min={0}
                defaultValue={value(patient.height)}
              />
            </FormField>
            <FormField label="Division" htmlFor="division">
              <Input
                id="division"
                name="division"
                defaultValue={value(patient.division)}
              />
            </FormField>
            <FormField label="District" htmlFor="zilla">
              <Input id="zilla" name="zilla" defaultValue={value(patient.zilla)} />
            </FormField>
            <FormField label="Address" htmlFor="address">
              <Input
                id="address"
                name="address"
                defaultValue={value(patient.address)}
              />
            </FormField>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader
          title="Vitals"
          description="Latest measurements recorded during a visit."
        />
        <CardBody>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <FormField label="Blood pressure" htmlFor="bloodPressure">
              <Input
                id="bloodPressure"
                name="bloodPressure"
                defaultValue={value(patient.bloodPressure)}
                placeholder="e.g. 120/80"
              />
            </FormField>
            <FormField label="Systolic BP" htmlFor="systolicBP">
              <Input
                id="systolicBP"
                name="systolicBP"
                type="number"
                defaultValue={value(patient.systolicBP)}
              />
            </FormField>
            <FormField label="Diastolic BP" htmlFor="diastolicBP">
              <Input
                id="diastolicBP"
                name="diastolicBP"
                type="number"
                defaultValue={value(patient.diastolicBP)}
              />
            </FormField>
            <FormField label="Heart rate" htmlFor="heartRate">
              <Input
                id="heartRate"
                name="heartRate"
                type="number"
                defaultValue={value(patient.heartRate)}
              />
            </FormField>
            <FormField label="Oxygen saturation (%)" htmlFor="oxygenSaturation">
              <Input
                id="oxygenSaturation"
                name="oxygenSaturation"
                type="number"
                defaultValue={value(patient.oxygenSaturation)}
              />
            </FormField>
            <FormField label="Respiratory rate" htmlFor="respiratoryRate">
              <Input
                id="respiratoryRate"
                name="respiratoryRate"
                type="number"
                step="0.1"
                defaultValue={value(patient.respiratoryRate)}
              />
            </FormField>
            <FormField label="Body temperature (°F)" htmlFor="bodyTemperature">
              <Input
                id="bodyTemperature"
                name="bodyTemperature"
                type="number"
                step="0.1"
                defaultValue={value(patient.bodyTemperature)}
              />
            </FormField>
            <FormField label="BMI" htmlFor="bmi">
              <Input
                id="bmi"
                name="bmi"
                type="number"
                step="0.1"
                defaultValue={value(patient.bmi)}
              />
            </FormField>
            <FormField label="BMR" htmlFor="bmr">
              <Input
                id="bmr"
                name="bmr"
                type="number"
                step="0.1"
                defaultValue={value(patient.bmr)}
              />
            </FormField>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader
          title="Laboratory results"
          description="Blood, lipid and liver panel values."
        />
        <CardBody>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {[
              ["hb", "Hemoglobin (g/dL)", "0.1"],
              ["rbc", "RBC (million/µL)", "0.1"],
              ["wbc", "WBC (cells/µL)", "1"],
              ["platelet", "Platelets (cells/µL)", "1"],
              ["sugarLevel", "Sugar level", "0.1"],
              ["bloodSugarLevel", "Blood sugar level", "0.1"],
              ["cholesterolTotal", "Cholesterol total", "0.1"],
              ["cholesterolLDL", "LDL cholesterol", "0.1"],
              ["cholesterolHDL", "HDL cholesterol", "0.1"],
              ["triglyceride", "Triglycerides", "0.1"],
              ["creatinine", "Creatinine", "0.1"],
              ["creatinineLevel", "Creatinine level", "0.1"],
              ["alt", "ALT (SGPT)", "0.1"],
              ["ast", "AST (SGOT)", "0.1"],
              ["bilirubin", "Bilirubin", "0.1"],
              ["cholesterol", "Cholesterol (criteria)", "0.1"],
              ["bloodSugar", "Blood sugar (criteria)", "0.1"],
            ].map(([field, label, step]) => (
              <FormField key={field} label={label} htmlFor={field}>
                <Input
                  id={field}
                  name={field}
                  type="number"
                  step={step}
                  defaultValue={value(
                    patient[field as keyof DoctorPatientRecord] as unknown as
                      | number
                      | null
                      | undefined,
                  )}
                />
              </FormField>
            ))}
            <FormField label="Rh factor" htmlFor="rhFactor">
              <Input
                id="rhFactor"
                name="rhFactor"
                defaultValue={value(patient.rhFactor)}
              />
            </FormField>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader
          title="Medical history"
          description="Comma-separated lists; clear a field to remove all entries."
        />
        <CardBody>
          <div className="grid gap-4 sm:grid-cols-2">
            <FormField label="Allergies" htmlFor="allergies">
              <Textarea
                id="allergies"
                name="allergies"
                defaultValue={csv(patient.allergies)}
              />
            </FormField>
            <FormField label="Diseases" htmlFor="diseases">
              <Textarea
                id="diseases"
                name="diseases"
                defaultValue={csv(patient.diseases)}
              />
            </FormField>
            <FormField label="Medications" htmlFor="medications">
              <Textarea
                id="medications"
                name="medications"
                defaultValue={csv(patient.medications)}
              />
            </FormField>
            <FormField label="Symptoms" htmlFor="symptoms">
              <Textarea
                id="symptoms"
                name="symptoms"
                defaultValue={csv(patient.symptoms)}
              />
            </FormField>
            <FormField label="Family disease history" htmlFor="familyDiseaseHistory">
              <Textarea
                id="familyDiseaseHistory"
                name="familyDiseaseHistory"
                defaultValue={csv(patient.familyDiseaseHistory)}
              />
            </FormField>
            <FormField label="Vaccination history" htmlFor="vaccinationHistory">
              <Textarea
                id="vaccinationHistory"
                name="vaccinationHistory"
                defaultValue={csv(patient.vaccinationHistory)}
              />
            </FormField>
            <FormField label="Past surgeries" htmlFor="pastSurgeries">
              <Textarea
                id="pastSurgeries"
                name="pastSurgeries"
                defaultValue={csv(patient.pastSurgeries)}
              />
            </FormField>
            <FormField label="Mental health conditions" htmlFor="mentalDiseases">
              <Textarea
                id="mentalDiseases"
                name="mentalDiseases"
                defaultValue={csv(patient.mentalDiseases)}
              />
            </FormField>
          </div>
        </CardBody>
      </Card>

      <Card>
        <CardHeader
          title="Lifestyle & wellbeing"
          description="Habits and mental-health screening scores."
        />
        <CardBody>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <FormField label="Smoker" htmlFor="smoker">
              <Select
                id="smoker"
                name="smoker"
                defaultValue={patient.smoker ? "true" : "false"}
              >
                <option value="false">No</option>
                <option value="true">Yes</option>
              </Select>
            </FormField>
            <FormField label="Alcohol" htmlFor="alcoholConsumer">
              <Select
                id="alcoholConsumer"
                name="alcoholConsumer"
                defaultValue={patient.alcoholConsumer ? "true" : "false"}
              >
                <option value="false">No</option>
                <option value="true">Yes</option>
              </Select>
            </FormField>
            <FormField label="Physical activity (days/week)" htmlFor="physicalActivity">
              <Input
                id="physicalActivity"
                name="physicalActivity"
                type="number"
                min={0}
                max={7}
                defaultValue={value(patient.physicalActivity)}
              />
            </FormField>
            <FormField label="Sleep (hours)" htmlFor="sleepHours">
              <Input
                id="sleepHours"
                name="sleepHours"
                type="number"
                step="0.1"
                defaultValue={value(patient.sleepHours)}
              />
            </FormField>
            <FormField label="Water intake (L/day)" htmlFor="waterIntake">
              <Input
                id="waterIntake"
                name="waterIntake"
                type="number"
                step="0.1"
                defaultValue={value(patient.waterIntake)}
              />
            </FormField>
            <FormField label="Stress level (0–10)" htmlFor="stressLevel">
              <Input
                id="stressLevel"
                name="stressLevel"
                type="number"
                min={0}
                max={10}
                defaultValue={value(patient.stressLevel)}
              />
            </FormField>
            <FormField label="Anxiety score (0–10)" htmlFor="anxietyScore">
              <Input
                id="anxietyScore"
                name="anxietyScore"
                type="number"
                min={0}
                max={10}
                defaultValue={value(patient.anxietyScore)}
              />
            </FormField>
          </div>
        </CardBody>
      </Card>

      {state ? (
        state.ok ? (
          <SuccessMessage>{state.message}</SuccessMessage>
        ) : (
          <ErrorMessage>{state.message}</ErrorMessage>
        )
      ) : null}

      <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-border bg-surface px-4 py-3">
        <p className="text-xs text-text-muted">
          The patient receives a live notification when you save.
        </p>
        <Button type="submit" disabled={pending}>
          {pending ? "Saving…" : "Save patient record"}
        </Button>
      </div>
    </form>
  );
}
