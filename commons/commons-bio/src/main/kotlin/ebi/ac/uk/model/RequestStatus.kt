package ebi.ac.uk.model

enum class RequestStatus {
    DRAFT,
    SUBMITTED,
    ACCEPTED,
    REQUESTED,
    INDEXED,
    LOADED,
    VALIDATED,
    INDEXED_CLEANED,
    CLEANED,
    FILES_COPIED,
    CHECK_RELEASED,
    PERSISTED,
    PROCESSED,
    INVALID,
    ;

    companion object {
        val DRAFT_STATUS: Set<RequestStatus> = setOf(DRAFT, SUBMITTED)
        val PROCESSED_STATUS: Set<RequestStatus> = setOf(PROCESSED)

        /**
         * List of status consider as the system is processing or owns the submission.
         */
        val PROCESSING_STATUS: Set<RequestStatus> =
            setOf(
                ACCEPTED,
                REQUESTED,
                INDEXED,
                LOADED,
                INDEXED_CLEANED,
                CLEANED,
                FILES_COPIED,
                CHECK_RELEASED,
                PERSISTED,
                INVALID,
            )
    }
}
