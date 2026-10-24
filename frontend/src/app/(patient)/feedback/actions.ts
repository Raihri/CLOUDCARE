"use server";

import { redirect } from "next/navigation";

import { springFetch } from "@/lib/api";

export type FeedbackState = { error?: string } | undefined;

/**
 * Submit feedback.
 *
 * The legacy form posts to /patient/feedbacks/save and redirects. We post
 * JSON to the same path, then redirect on success. Note the backend throws a
 * 500 (not a validation error) if rating is outside 1–5, so the rating is
 * clamped here rather than relying on server-side validation.
 */
export async function submitFeedback(
  _prev: FeedbackState,
  formData: FormData,
): Promise<FeedbackState> {
  const rating = Number(formData.get("rating") ?? 0);

  if (!Number.isFinite(rating) || rating < 1 || rating > 5) {
    return { error: "Please choose a rating from 1 to 5." };
  }

  const comments = String(formData.get("comments") ?? "").trim();
  if (!comments) {
    return { error: "Please tell us a little about your experience." };
  }

  let ok = false;
  try {
    const res = await springFetch("/patient/feedbacks/save", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        rating: String(rating),
        category: String(formData.get("category") ?? "General"),
        comments,
      }).toString(),
    });

    // The endpoint answers with a 302 redirect on success, which
    // springFetch surfaces unfollowed.
    ok = res.status === 302 || res.ok;
  } catch {
    return { error: "Could not submit your feedback. Please try again." };
  }

  // Outside the try block: redirect() signals via a thrown control-flow
  // exception, so calling it inside would be swallowed and misreported.
  if (ok) redirect("/feedback?sent=1");

  return { error: "Could not submit your feedback. Please try again." };
}