# ADR 0002: Keyset pagination for large file lists

## Status

Accepted

## Context

The extended file-list browse endpoint currently pages through results with offset-based pagination backed by Spring
Data `Pageable`. The underlying Mongo query is already filtered and ordered by the compound index on
`submissionAccNo`, `submissionVersion`, `fileListName`, and `index`, but deep pages still require MongoDB to advance
through every skipped index entry before it can return the requested rows.

This is acceptable for small and medium submissions, but it scales poorly once a file list reaches millions of rows.
The user-visible contract still needs deterministic ordering by file index.

## Decision

Use keyset pagination for file-list browsing, with the file `index` as the cursor boundary. The collection already
stores a stable, ordered `index` for every file in a submission version, and that field is covered by the existing
compound index. Cursor-based navigation therefore keeps each page request proportional to the page size instead of the
requested offset.

## Considered Options

- Keep offset pagination. This is the simplest option, but deep navigation remains O(offset) and gets slower as file
  lists grow.
- Precompute page buckets or page tables. This avoids large skips, but it adds storage, invalidation, and maintenance
  complexity for a shape that is otherwise already well indexed.
- Keyset pagination on `index`. This is the chosen direction because it matches the existing sort order and avoids
  traversing skipped rows.

## Consequences

- Deep pages become much cheaper because the database only needs to read the next matching rows.
- The browse API can no longer treat page number as an arbitrary random-access coordinate without extra compatibility
  logic.
- Next and previous links will need to carry a cursor boundary rather than a plain absolute offset.
- Link-list browsing has the same structural shape and may need the same treatment later.
