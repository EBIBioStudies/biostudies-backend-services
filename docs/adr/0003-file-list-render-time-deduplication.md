# File list render-time deduplication

## Status

Accepted

## Context

Some file lists contain duplicate entries with the same path and metadata. The extended file-list endpoint can also
surface the same file list more than once when it is referenced from multiple sections, which makes the duplication
visible to users as repeated identical rows.

The desired behavior is to treat identical file-list entries as a single logical file at render time while preserving
distinct entries when path or metadata differ. Pagination content must operate on the deduplicated view so that offsets
refer to what the user actually sees rather than to raw stored rows.

## Decision

Deduplicate file-list entries when rendering the extended file-list response, using file path plus metadata as the
identity of a visible entry. If the same file list is referenced multiple times, render only one logical set of
entries. Pagination content is based on the deduplicated sequence, while the total count remains the raw stored count.

Perform deduplication in the application while streaming file-list entries from persistence. Keep only the visible
identity of entries already seen, rather than materializing the complete file documents. For paginated responses,
collect only entries within the requested deduplicated offset and limit, then cancel the stream. Obtain the total with a
separate indexed count of stored rows rather than scanning the complete file list.

## Consequences

- The response content reflects the logical file list, while `totalElements` reports the raw stored row count.
- Pagination offsets must be calculated after deduplication, otherwise duplicate rows will shift the visible page
  boundaries.
- Identical duplicate rows no longer appear multiple times, but legitimately distinct rows with the same path remain
  visible when their metadata differs.
- Rendering retains one path-and-metadata identity per visible entry in application memory.
- Totals can overestimate visible entries and produce an empty trailing page when duplicates exist. This is an accepted
  corner-case tradeoff that avoids a complete request-time scan for submissions containing millions of files.
