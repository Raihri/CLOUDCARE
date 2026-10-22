import type { Metadata } from "next";

import { AppShell } from "@/components/layout/AppShell";
import { PageHeader } from "@/components/ui/PageHeader";
import { requireRole } from "@/lib/auth";
import { ChatPanel } from "./ChatPanel";

export const metadata: Metadata = { title: "CloudCare AI" };

export const instant = false;

export default async function AiChatPage() {
  const user = await requireRole(["PATIENT"], "/ai-chat");

  return (
    <AppShell user={user}>
      <PageHeader
        title="CloudCare AI"
        description="General health information and guidance. Not a diagnosis."
      />
      <ChatPanel displayName={user.name ?? "there"} />
    </AppShell>
  );
}