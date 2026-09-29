# ADR 0004: Cut over submission tickets to Request Tracker REST 2.0

## Status

Accepted

## Context

Submission notifications create and update Request Tracker tickets through REST 1.0. REST 2.0 uses JSON resources and distinguishes external ticket correspondence from internal comments.

## Decision

Use direct REST 2.0 ticket endpoints with the configured queue: create tickets through `POST /ticket` and send every later user notification through `POST /ticket/{id}/correspond`. Do not retain a REST 1.0 fallback. Ticket creation and correspondence keep the ticket status `resolved`, the submission accession custom field, and the operational notification recipient.

Retain the existing `commentTicket` method name in this migration, even though it performs REST 2.0 correspondence. Renaming the internal API is deferred to keep this change focused on the Request Tracker protocol migration.

## Consequences

- REST 2.0 provisioning errors fail the notification operation visibly instead of silently using REST 1.0.
- No ticket-data migration is required: persisted numeric ticket IDs continue to identify tickets in the same Request Tracker instance.
- The client sends JSON and obtains the created ticket ID from the REST 2.0 response rather than parsing REST 1.0 text.
- The method name `commentTicket` remains legacy terminology and must not be interpreted as a call to REST 2.0's internal `comment` endpoint.
