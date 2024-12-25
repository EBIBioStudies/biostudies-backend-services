package ac.uk.ebi.biostd.persistence.common.exception

class SubmissionRequestDraftNotFoundException(
    key: String,
    owner: String,
) : RuntimeException("The submission request draft with key $key was not found for owner $owner")
