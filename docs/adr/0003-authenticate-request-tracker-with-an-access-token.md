# ADR 0003: Authenticate Request Tracker with an access token

## Status

Accepted

## Context

The submission notification service currently authenticates its Request Tracker REST 1.0 calls with a configured username and password. REST 2.0 supports Basic authentication but Request Tracker recommends an access token for API clients.

## Decision

Configure the notification service with one Request Tracker access token and send it in the REST 2.0 `Authorization` header. The token belongs to a service account with only the permissions needed to create submission tickets and add ticket correspondence.

## Consequences

- Deployment configuration must provide an RT access token instead of an RT username and password.
- Token rotation no longer requires changing a service-account password.
- The Request Tracker administrators must create and provision the token before deployment.
