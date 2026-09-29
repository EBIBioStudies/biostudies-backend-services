# Context

## Glossary

### Submissions
- **Public collection statistics**: Monthly collection-level data-size, month-to-month change, and study-count
  measures that include only submissions marked released when the snapshot is taken. It describes report contents,
  not endpoint access.
  _Avoid_: Public studies, published report
- **Statistical collection category**: A report grouping based on a submission's collection membership. ArrayExpress
  is its own category and is also part of the non-imaging category; BioImages is excluded from non-imaging.
  _Avoid_: Mutually exclusive collection category
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
- **Accession ADMIN permission**: An `ADMIN` access permission assigned to a user for one accession. It authorizes
  that user to grant permissions for that same accession, without granting administrative authority elsewhere.
- **Email-change transfer**: The atomic administrative update of a user account's email, including the account's
  transferable submission resources while preserving the active account identity and user space.
- **Transfer log**: An immutable administrative audit record of a transfer or email update, identifying when it
  occurred, the acting account, the source email, the target email, and the operation type; one is recorded for each
  successful command even when no submission ownership changes.

### Notifications
- **Ticket correspondence**: A user-facing message recorded on a submission's Request Tracker ticket and delivered through its external communication channel.
  _Avoid_: Internal comment, ticket note
- **Resolved submission ticket**: A submission's Request Tracker ticket whose lifecycle status is `resolved` after its creation and after each notification correspondence.
  _Avoid_: Open support ticket
- **Submission ticket accession**: The submission accession stored in a Request Tracker ticket's `Accession` custom field, linking the ticket to exactly one submission.
  _Avoid_: Ticket subject identifier
- **Operational notification recipient**: The mailbox that receives a copy of every submission ticket correspondence for operational visibility.
  _Avoid_: Per-message BCC recipient
- **Security notification**: an account-related notification used for activation, activation by email, and password 
  reset.
- **Urgent notification**: a separate operational notification used to report failures in the security notification 
  flow; it carries an error type and an error message.
