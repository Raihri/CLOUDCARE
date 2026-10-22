/**
 * Blood groups CloudCare accepts.
 *
 * Lives outside any "use server" file deliberately: a module with that
 * directive may only export async functions, so a plain array exported from a
 * Server Action module arrives on the client as a function-reference
 * placeholder rather than the array — which fails at render time with
 * "BLOOD_GROUPS.map is not a function".
 */
export const BLOOD_GROUPS = [
  "A+",
  "A-",
  "B+",
  "B-",
  "AB+",
  "AB-",
  "O+",
  "O-",
] as const;