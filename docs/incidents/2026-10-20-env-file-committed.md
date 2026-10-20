# Postmortem: .env committed to public repo

**Found:** 2026-10-20 · **Severity:** Low · **Status:** Mitigated

## What happened
`.env` with local Postgres credentials was committed on 2026-02-14 (`7f21c29`)
and untracked on 2026-02-15 (`2926fb2`), but it remains in the public git history.

## Impact
None. The databases only run locally in Docker, and the passwords were not reused.

## Why
- `.env` was not in `.gitignore` when it was created.
- No secret scanning was in place. A later `gitleaks` scan also missed it,
  because the passwords were simple, low-entropy values.

## Resolution
Local credentials rotated.
