# Context

## Glossary

### Submissions
- **Source File**: The original request-file object from which a submission request file was taken before submission 
  processing persisted, copied, released, or otherwise transformed it.
- **Request file source type**: The origin of a file within a specific submission request. A file from user space is 
  `USER`; a file carried forward from an earlier submission version is `SUBMISSION`.
- **User space**: The per-user file area used as an input source for submission files uploaded by a user before they are 
  consumed by a completed submission.
- **Stuck submission request**: A submission request that has remained in a processing state without modification beyond
  the retry threshold.
- **Active submission-processing lock**: An unexpired claim that a process is working on one submission request. Its
  presence is authoritative for retry eligibility, independently of the process's external cluster status.

### Notifications
- **Security notification**: an account-related notification used for activation, activation by email, and password 
  reset.
- **Urgent notification**: a separate operational notification used to report failures in the security notification 
  flow; it carries an error type and an error message.
