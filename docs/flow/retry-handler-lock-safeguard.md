# Retry handler lock safeguard

## Agreed behaviour

The retry handler considers a submission request eligible when both conditions hold:

1. the request has remained in a processing state without modification for more than three hours; and
2. the request has no unexpired submission-processing lock.

An unexpired lock is authoritative. The retry handler does not query the external cluster to verify the lock owner's
job status. The eligibility check is best-effort: a process may acquire a lock between the lookup and retry dispatch,
while the existing per-stage locking continues to protect submission state from concurrent mutation.

Apply the same rule when retrying at application startup and from the three-hour schedule. Both entry points should
delegate to one shared retry flow.

## Persistence boundary

Add a bulk operation to `SubmissionRequestPersistenceService` with a submission-oriented contract:

```kotlin
suspend fun getActiveSubmissionLocks(
    submissionIds: Collection<SubmissionId>,
): Set<SubmissionId>
```

The Mongo implementation translates each `SubmissionId` to the existing `REQUEST_<accNo>_<version>` lock identifier
and returns only identifiers whose matching lock expires in the future. Mongo collection details and lock identifiers
remain hidden behind the persistence boundary.

For every existing batch of up to 500 stuck requests, perform one bulk lock lookup, subtract the active locks, and
dispatch only the remaining requests. Do not dispatch an empty batch.

## Observability

Log one summary per retry run containing:

- the number of stuck candidates;
- the number retried; and
- the number skipped because of active locks.

## Verification

Cover both startup and scheduled execution, including:

- all candidates unlocked;
- a mixture of locked and unlocked candidates;
- all candidates locked;
- expired locks treated as inactive;
- candidate sets spanning more than one batch; and
- bulk lock lookup and retry dispatch receiving the expected identifiers.

## Future enhancement

Define explicit fail-closed behaviour for lock lookup failures. A future change may skip candidates whose lock state
cannot be determined, log their identifiers, and reconsider them on the next scheduled run. This failure policy is
outside the current safeguard.
