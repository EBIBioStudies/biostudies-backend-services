package ac.uk.ebi.biostd.persistence.common.exception

import ebi.ac.uk.model.RequestStatus

class SubmissionRequestDraftNotFoundException(
    key: String,
    owner: String,
    status: RequestStatus,
) : RuntimeException("The submission request draft with key $key and status $status was not found for owner $owner")
