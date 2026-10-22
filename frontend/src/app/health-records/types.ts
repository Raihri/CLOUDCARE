export type HealthRecord = {
  success: boolean;
  name: string | null;
  email: string | null;
  photoUrl: string | null;

  bloodGroup: string | null;
  age: number | null;
  gender: string | null;
  division: string | null;
  zilla: string | null;
  address: string | null;
  weight: number | null;
  height: number | null;
  bmi: number | null;
  nationalId: string | null;
  emergencyContact: string | null;
  fatherName: string | null;
  motherName: string | null;
  maritalStatus: string | null;
  dateOfBirth: string | null;
  lastDonated: string | null;

  systolicBP: number | null;
  diastolicBP: number | null;
  heartRate: number | null;
  oxygenSaturation: number | null;
  respiratoryRate: number | null;
  bodyTemperature: number | null;
  bloodSugarLevel: number | null;

  sleepHours: number | null;
  waterIntake: number | null;
  physicalActivity: number | null;
  smoker: boolean;
  alcoholConsumer: boolean;

  pregnant: boolean | null;
  breastFeeding: boolean | null;
  menstruationRegular: boolean | null;
  pregnancyWeek: number | null;
  lastMenstrualDate: string | null;

  parentalCareState: string | null;
  homeEnvironment: string | null;
  haveBeenBullied: boolean | null;
  feelingLeftOut: boolean | null;
};