# Deduplicate referenced-list entry storage

Within a submission version, each distinct file-list or link-list entry is persisted once per full list path, even when several sections reference that list or its source contains identical rows. Section references remain independent, while records sharing a path or URL but differing in metadata remain distinct and retain first-seen order; this avoids multiplying the list-entry collections while preserving valid PageTab semantics.
