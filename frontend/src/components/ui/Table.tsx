"use client";

import { useState, type ReactNode } from "react";

import { Button } from "@/components/ui/Button";
import { EmptyState } from "@/components/ui/Feedback";

export type Column<T> = {
  key: string;
  header: string;
  render: (row: T) => ReactNode;
  className?: string;
};

/**
 * Table with client-side pagination. The old app had tables but zero
 * pagination, so long admin/donor lists rendered in full.
 */
export function DataTable<T>({
  columns,
  rows,
  rowKey,
  emptyTitle = "Nothing here yet",
  emptyDescription,
  pageSize = 10,
}: {
  columns: Column<T>[];
  rows: T[];
  rowKey: (row: T) => string | number;
  emptyTitle?: string;
  emptyDescription?: string;
  pageSize?: number;
}) {
  const [page, setPage] = useState(0);

  if (rows.length === 0) {
    return <EmptyState title={emptyTitle} description={emptyDescription} />;
  }

  const pageCount = Math.ceil(rows.length / pageSize);
  const safePage = Math.min(page, pageCount - 1);
  const start = safePage * pageSize;
  const visible = rows.slice(start, start + pageSize);

  return (
    <div>
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border bg-neutral-50">
              {columns.map((col) => (
                <th
                  key={col.key}
                  className={`px-4 py-2.5 text-left font-medium text-text-secondary ${col.className ?? ""}`}
                >
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {visible.map((row) => (
              <tr
                key={rowKey(row)}
                className="border-b border-border last:border-0 hover:bg-neutral-50"
              >
                {columns.map((col) => (
                  <td
                    key={col.key}
                    className={`px-4 py-3 text-text-primary ${col.className ?? ""}`}
                  >
                    {col.render(row)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {pageCount > 1 ? (
        <div className="flex items-center justify-between border-t border-border px-4 py-3">
          <p className="text-xs text-text-muted">
            Showing {start + 1}–{Math.min(start + pageSize, rows.length)} of{" "}
            {rows.length}
          </p>
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="secondary"
              disabled={safePage === 0}
              onClick={() => setPage(safePage - 1)}
            >
              Previous
            </Button>
            <span className="text-xs text-text-muted">
              {safePage + 1} / {pageCount}
            </span>
            <Button
              size="sm"
              variant="secondary"
              disabled={safePage >= pageCount - 1}
              onClick={() => setPage(safePage + 1)}
            >
              Next
            </Button>
          </div>
        </div>
      ) : null}
    </div>
  );
}