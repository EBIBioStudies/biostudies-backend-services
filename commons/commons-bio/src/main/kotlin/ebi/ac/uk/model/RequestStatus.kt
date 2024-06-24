package ebi.ac.uk.model

enum class RequestStatus {
    REQUESTED,
    INDEXED,
    LOADED,
    INDEXED_CLEANED,
    CLEANED,
    FILES_COPIED,
    CHECK_RELEASED,
    PERSISTED,
    PROCESSED,
    ;

    companion object {
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
            )
    }
}
