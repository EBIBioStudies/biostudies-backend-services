# ADR 0001: Automatic user space cleanup

## Status

Proposed

## Context

BioStudies tracks each user's `lastActivity` in SQL. Cleanup notification logic already uses
`ApplicationProperties.cleanUp.cleanUpPeriodDays` and the warning day properties to notify users whose private
workspace has been inactive for configured periods.

The web application already has a daily scheduled cleanup notification process in `OperationsScheduler`, and the
submission task application can run cleanup notification work remotely through `RemoteSubmitterExecutor` and the
cluster client.

The missing behavior is automatic deletion of files from inactive user spaces. The requirement is:

- run once per day;
- find users whose `lastActivity` is at least `cleanUpPeriodDays` days old;
- dispatch a data-mover cluster job for each matching user;
- delete all files in that user's user space.

## Decision

Use the existing cleanup boundary and cluster task infrastructure:

- extend `LocalUserSpaceCleanUpService` with two responsibilities:
  - discover active users eligible for cleanup using an exact cutoff day derived from
    `today - cleanUpPeriodDays`;
  - skip users whose user space is empty;
  - submit one data-mover cluster job per eligible user;
- extend `ExtUserSpaceCleanUpService.CleanUpMode.CLEAN_UP` to:
  - execute the cleanup flow locally when invoked by the submission task;
  - execute the cleanup flow remotely through a submission-task cluster job when invoked by the web scheduler;
- schedule cleanup separately from notifications in `OperationsScheduler`, behind `app.cleanup.enabled`, close to the
  end of the day.

Cleanup deletes the contents of `SecurityUser.userFolder.path`, including nested and hidden files, while preserving the
root user folder itself.

The deletion task does not re-check eligibility before deleting. Users receive three warning emails before the cleanup
date, and the cleanup job is intentionally scheduled near the end of that date.

Add Mongo audit documents alongside the existing notification audit documents:

- `DocCleanUpLog` for successful scheduler dispatch records, including:
  - exact date-time when cleanup is started at scheduler dispatch time;
  - cluster job id;
  - user email;
  - user `lastActivity` at the time cleanup is selected;
  - absolute path to the user space.
- `DocCleanUpError` for any error while trying to cleanup, including:
  - error message;
  - cluster job id when available;
  - user email;
  - absolute path to the user space.

Create `cleanup_logs` and `cleanup_errors` collections through the Mongo migration flow in `DatabaseChangeLog.kt`,
following the `notification_logs` and `notification_errors` pattern. Add a background index on `email` only for each
cleanup collection.

Use `LocalDate.now()` for cutoff calculation, matching the existing cleanup notification implementation.

## Consequences

- The web scheduler remains the orchestrator and the cluster performs filesystem deletion.
- Per-user jobs isolate failures and make logs easier to correlate to one account.
- The task must be idempotent: an empty or missing folder should not cause repeated harmful failures.
- Only active users are considered for cleanup.
- Users with empty user spaces are skipped and no cleanup job is dispatched for them.
- Exact cutoff discovery avoids repeatedly dispatching cleanup jobs for older users on every daily run.
- Failed cleanup attempts are captured in Mongo for later operational decisions.
- One cluster job per user is accepted because the expected matching population per day is small.
