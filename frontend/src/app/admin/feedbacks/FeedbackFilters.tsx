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
    <form onSubmit={apply} className="flex items-end gap-3">
      <FormField label="Name" htmlFor="name">
        <Input
          id="name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="w-48"
        />
      </FormField>
      <FormField label="Email" htmlFor="email">
        <Input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="w-56"
        />
      </FormField>
      <FormField label="Category" htmlFor="category">
        <Input
          id="category"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          className="w-40"
        />
      </FormField>
      <Button type="submit">Filter</Button>
      <button
        type="button"
        onClick={clear}
        className="h-10 px-3 text-sm font-medium text-highlight hover:underline"
      >
        Clear
      </button>
    </form>
  );
}