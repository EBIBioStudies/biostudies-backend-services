package ebi.ac.uk.model

enum class RequestStatus {
    DRAFT,
    SUBMITTED,
    REQUESTED,
    FILES_VALIDATED,
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
        val DRAFT_STATUS: Set<RequestStatus> = setOf(DRAFT)
        val PROCESSED_STATUS: Set<RequestStatus> = setOf(PROCESSED)

        val EDITABLE_STATUS: Set<RequestStatus> = setOf(DRAFT, INVALID)

        /**
         * List of status consider as the system is processing or owns the submission.
         */
        val PROCESSING_STATUS: Set<RequestStatus> =
            setOf(
                SUBMITTED,
                REQUESTED,
                FILES_VALIDATED,
                INDEXED,
                LOADED,
                INDEXED_CLEANED,
                CLEANED,
                FILES_COPIED,
                CHECK_RELEASED,
                PERSISTED,
            )

        /**
         * List of status as request are consider active to the user.
         */
        val ACTIVE_STATUS: Set<RequestStatus> =
            buildSet {
                addAll(PROCESSING_STATUS)
                add(INVALID)
            }
    }
}
