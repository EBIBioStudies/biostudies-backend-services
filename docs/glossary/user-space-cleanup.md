# User space cleanup glossary

## User space

The private upload workspace associated with a BioStudies user. In code this is exposed through
`SecurityUser.userFolder`.

## Last activity

The `DbUser.lastActivity` timestamp updated when a user authenticates or performs tracked security activity. Cleanup
eligibility is calculated from this value.

## Cleanup period

The configured number of days after `lastActivity` when a user's private workspace becomes eligible for deletion.
Configured as `app.cleanup.cleanUpPeriodDays`.

## Cleanup notification

An email warning sent before deletion. Existing warning thresholds are configured by `firstWarningDays`,
`secondWarningDays`, and `thirdWarningDays`.

## Cleanup discovery

The daily process that finds active users whose `lastActivity` falls exactly on the cleanup cutoff day and whose user
space is not empty.

## Cleanup dispatch

The orchestration step that submits one data-mover cluster job per eligible user.

## Cleanup task

The cluster-executed command that deletes files for a single user's workspace.

## Root user folder

The directory represented by `SecurityUser.userFolder.path`. The proposed cleanup deletes the contents of this
directory, not the directory itself, unless the policy is changed explicitly.

## Cleanup log

Mongo document proposed as `DocCleanUpLog`. Records scheduler dispatch information, including cleanup date-time,
cluster job id, user email, the user's `lastActivity` at selection time, and the absolute path to the user space. The
`cleanup_logs` collection has an `email` index.

## Cleanup error

Mongo document proposed as `DocCleanUpError`. Records failures that happen while trying to cleanup a user's workspace,
including the error message, cluster job id when available, user email, and absolute path to the user space. The
`cleanup_errors` collection has an `email` index.

## Exact cutoff

The cleanup policy where only users whose `lastActivity` falls on `today - cleanUpPeriodDays` are selected. Older users
are not repeatedly selected on every later daily run. The cutoff uses `LocalDate.now()`, matching the existing cleanup
notification implementation.

## Empty user space

A user folder with no files to delete. These users are skipped during cleanup discovery and no cluster cleanup job is
dispatched for them.
