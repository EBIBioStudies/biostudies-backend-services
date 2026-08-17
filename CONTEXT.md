# Context

## Glossary

### Submissions
- **File-list reference**: A section's reference to a file list identified by its full PageTab path. Multiple sections
  may reference the same file list within one submission version.
- **File-list entry**: A distinct parsed file record belonging to a full file-list path in one submission version. It
  exists once regardless of how many file-list references use that list or how often the identical record appears.
- **Link-list reference**: A section's reference to a link list identified by its full PageTab path. Multiple sections
  may reference the same link list within one submission version.
- **Link-list entry**: A distinct parsed link record belonging to a full link-list path in one submission version. It
  exists once regardless of how many link-list references use that list or how often the identical record appears.
- **Source File**: The original request-file object from which a submission request file was taken before submission 
  processing persisted, copied, released, or otherwise transformed it.
- **Request file source type**: The origin of a file within a specific submission request. A file from user space is 
  `USER`; a file carried forward from an earlier submission version is `SUBMISSION`.
- **User space**: The per-user file area used as an input source for submission files uploaded by a user before they are 
  consumed by a completed submission.
- **Direct-upload staging area**: The `direct-uploads` folder in user space containing a PageTab file received through
  a direct file-submission request. It is a first-priority source for that PageTab, alongside the normal user-space
  source used by its other file references.
- **Stuck submission request**: A submission request that has remained in a processing state without modification beyond
  the retry threshold.
- **Active submission-processing lock**: An unexpired claim that a process is working on one submission request. Its
  presence is authoritative for retry eligibility, independently of the process's external cluster status.

### User accounts
- **Email-change transfer**: The atomic administrative update of a user account's email, including the account's
  transferable submission resources while preserving the active account identity and user space.
- **Transfer log**: An immutable administrative audit record of a transfer or email update, identifying when it
  occurred, the acting account, the source email, the target email, and the operation type; one is recorded for each
  successful command even when no submission ownership changes.

### Notifications
- **Security notification**: an account-related notification used for activation, activation by email, and password 
  reset.
- **Urgent notification**: a separate operational notification used to report failures in the security notification 
  flow; it carries an error type and an error message.
