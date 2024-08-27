package ebi.ac.uk.model

enum class RequestStatus {
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
        /**
         * List of status consider as the system is processing or owns the submission.
         */
        val PROCESSING: Set<RequestStatus> =
            setOf(
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
