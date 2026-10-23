"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

import { Button } from "@/components/ui/Button";
import { FormField, Input } from "@/components/ui/Form";

/**
 * Filter form for the feedback list.
 *
 * Filtering happens server-side (the backend applies the same predicates), so
 * applying pushes the values into the URL rather than filtering in the browser.
 * That keeps the result shareable and keeps the filtering logic in one place.
 */
export function FeedbackFilters({
  current,
}: {
  current: { name?: string; email?: string; category?: string };
}) {
  const router = useRouter();
  const [name, setName] = useState(current.name ?? "");
  const [email, setEmail] = useState(current.email ?? "");
  const [category, setCategory] = useState(current.category ?? "");

  function apply(event: React.FormEvent) {
    event.preventDefault();
    const params = new URLSearchParams();
    if (name) params.set("name", name);
    if (email) params.set("email", email);
    if (category) params.set("category", category);
    const qs = params.toString();
    router.push(qs ? `/admin/feedbacks?${qs}` : "/admin/feedbacks");
  }

  function clear() {
    setName("");
    setEmail("");
    setCategory("");
    router.push("/admin/feedbacks");
  }

  return (
    <form
      onSubmit={apply}
      className="rounded-xl border border-border bg-surface p-4 shadow-sm"
    >
      <div className="mb-3 flex items-center gap-2 text-sm font-medium text-text-primary">
        <i
          aria-hidden
          className="bx bx-filter-alt text-base leading-none text-primary-600"
        />
        Filters
      </div>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-[1fr_1fr_1fr_auto] lg:items-end">
        <FormField label="Name" htmlFor="name">
          <Input
            id="name"
            value={name}
            onChange={(event) => setName(event.target.value)}
            placeholder="Any name"
          />
        </FormField>

        <FormField label="Email" htmlFor="email">
          <Input
            id="email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="Any email"
          />
        </FormField>

        <FormField label="Category" htmlFor="category">
          <Input
            id="category"
            value={category}
            onChange={(event) => setCategory(event.target.value)}
            placeholder="Any category"
          />
        </FormField>

        <div className="flex items-center gap-2">
          <Button type="submit" className="flex-1 lg:flex-none">
            Filter
          </Button>
          <Button type="button" variant="ghost" onClick={clear}>
            Clear
          </Button>
        </div>
      </div>
    </form>
  );
}
