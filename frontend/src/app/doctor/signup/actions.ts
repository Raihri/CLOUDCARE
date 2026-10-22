"use server";

export type DoctorSignupState = { error?: string } | undefined;

/**
 * Submit a doctor application.
 *
 * Spring expects multipart/form-data with two required files
 * (profileImageFile, certificateFile), so this builds FormData and must NOT set
 * a Content-Type header — the runtime adds the multipart boundary itself.
 */
export async function applyAsDoctor(
  _prev: DoctorSignupState,
  formData: FormData,
): Promise<DoctorSignupState> {
  const name = String(formData.get("name") ?? "").trim();
  const email = String(formData.get("email") ?? "").trim();
  const password = String(formData.get("password") ?? "");
  const bmdcRegNo = String(formData.get("bmdcRegNo") ?? "").trim();

  if (!name || !email || !password || !bmdcRegNo) {
    return { error: "Name, email, password and BMDC registration number are required." };
  }
  if (password.length < 6) {
    return { error: "Password must be at least 6 characters." };
  }

  const profile = formData.get("profileImageFile");
  const certificate = formData.get("certificateFile");

  if (!(profile instanceof File) || profile.size === 0) {
    return { error: "A profile photo is required." };
  }
  if (!(certificate instanceof File) || certificate.size === 0) {
    return { error: "A supporting certificate is required." };
  }

  const base = process.env.SPRING_API_URL ?? "http://localhost:8080";

  // Pass the incoming files straight through; re-wrapping File objects in a
  // new FormData is unnecessary and risks losing the filename.
  const payload = new FormData();
  payload.set("name", name);
  payload.set("email", email);
  payload.set("password", password);
  payload.set("bmdcRegNo", bmdcRegNo);
  payload.set("specialization", String(formData.get("specialization") ?? ""));
  payload.set("degrees", String(formData.get("degrees") ?? ""));
  payload.set("phoneNumber", String(formData.get("phoneNumber") ?? ""));
  payload.set("profileImageFile", profile);
  payload.set("certificateFile", certificate);

  const res = await fetch(`${base}/doctor/signup/request`, {
    method: "POST",
    body: payload,
    redirect: "manual",
    cache: "no-store",
  });

  if (res.status === 302) {
    const location = res.headers.get("location") ?? "";
    if (location.includes("/doctor/login")) {
      return {};
    }
  }

  const html = await res.text();
  const backendError = extractError(html);
  return { error: backendError ?? "Could not submit your application." };
}

function extractError(html: string): string | null {
  const match =
    /class="error[^"]*"[^>]*>([\s\S]{0,300}?)<\/div>/i.exec(html) ??
    /th:text="\$\{error\}"[^>]*>([\s\S]{0,300}?)<\//i.exec(html);

  if (!match) return null;

  return match[1]
    .replace(/<[^>]+>/g, " ")
    .replace(/\s+/g, " ")
    .trim()
    .slice(0, 200);
}