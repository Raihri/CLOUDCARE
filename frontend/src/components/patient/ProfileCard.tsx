import type { HealthRecord } from "@/lib/health-record";

/**
 * The patient profile header — cover, photo, name and the handful of facts a
 * person would recognise at a glance.
 *
 * Deliberately small. Everything clinical (vitals, lifestyle, mental health,
 * biochemistry) lives on its own page at /health-metrics, so this stays a
 * profile rather than a data dump.
 */

const HEADING = "#22336b";

function initials(name: string | null, email: string | null): string {
  const source = (name ?? email ?? "").trim();
  if (!source) return "?";
  const parts = source.split(/[\s@._-]+/).filter(Boolean);
  if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
  return source.slice(0, 2).toUpperCase();
}

const GENDER_LABEL: Record<string, string> = {
  M: "Male",
  F: "Female",
  O: "Other",
  U: "Prefer not to say",
};

function genderLabel(value: string | null): string {
  if (!value) return "Not set";
  return GENDER_LABEL[value] ?? value;
}

/** Small pill under the name — the four facts worth seeing immediately. */
function Fact({
  icon,
  children,
}: {
  icon: string;
  children: React.ReactNode;
}) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full bg-primary-50 px-2.5 py-1 text-xs font-medium text-text-secondary">
      <i className={`bx ${icon} text-sm text-highlight`} aria-hidden />
      {children}
    </span>
  );
}

export function ProfileCard({ record }: { record: HealthRecord }) {
  const displayName = record.name ?? "Unknown Patient";

  const location = [record.zilla, record.address].filter(Boolean).join(", ");

  return (
    <div
      className="patient-profile-container w-full max-w-[1100px] overflow-hidden rounded-[18px] bg-surface shadow-md"
      style={{ animation: "profileFadeInUp 0.5s ease both" }}
    >
      {/* ---- Cover ---- */}
      <div
        className="h-28 w-full sm:h-36"
        style={{
          background: "linear-gradient(135deg, #4e7fff 0%, #2a7de1 45%, #22336b 100%)",
        }}
      />

      <div className="flex flex-col gap-4 px-6 pb-5 sm:flex-row sm:items-end sm:gap-6 sm:px-8 sm:pb-6">
        {/* ---- Photo, overlapping the cover ---- */}
        <div className="-mt-14 shrink-0 sm:-mt-16">
          {record.photoUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={record.photoUrl}
              alt={`${displayName}'s photo`}
              className="size-[104px] rounded-xl border-4 border-white object-cover shadow-md transition-transform duration-200 hover:scale-[1.03] sm:size-[120px]"
            />
          ) : (
            <div
              className="flex size-[104px] items-center justify-center rounded-xl border-4 border-white text-3xl font-bold text-white shadow-md sm:size-[120px] sm:text-4xl"
              style={{ background: "linear-gradient(180deg,#4e7fff,#22336b)" }}
              aria-label="Patient photo placeholder"
            >
              {initials(record.name, record.email)}
            </div>
          )}
        </div>

        {/* ---- Name + the few facts worth showing ---- */}
        <div className="min-w-0 flex-1 pt-2 sm:pt-0">
          <h1
            className="truncate text-[1.6rem] font-bold sm:text-[2rem]"
            style={{ color: HEADING }}
          >
            {displayName}
          </h1>

          <div className="mt-2 flex flex-wrap gap-2">
            {record.bloodGroup ? (
              <span className="inline-flex items-center gap-1.5 rounded-full bg-blood-soft px-2.5 py-1 text-xs font-semibold text-blood-dark">
                <i className="bx bxs-droplet text-sm" aria-hidden />
                {record.bloodGroup}
              </span>
            ) : null}

            {record.age != null ? (
              <Fact icon="bx-user">{record.age} years</Fact>
            ) : null}

            {record.gender ? (
              <Fact icon="bxs-user">{genderLabel(record.gender)}</Fact>
            ) : null}

            {location ? <Fact icon="bx-map-alt">{location}</Fact> : null}
          </div>
        </div>
      </div>
    </div>
  );
}