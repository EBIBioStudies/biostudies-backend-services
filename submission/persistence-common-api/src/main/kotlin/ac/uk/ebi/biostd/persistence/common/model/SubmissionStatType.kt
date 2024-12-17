package ac.uk.ebi.biostd.persistence.common.model

enum class SubmissionStatType(
    val value: String,
) {
    VIEWS("VIEWS"),
    FILES_SIZE("FILES_SIZE"),
    DIRECTORIES("DIRECTORIES"),
    EMPTY_DIRECTORIES("EMPTY_DIRECTORIES"),
    ;

    companion object {
        fun fromString(value: String): SubmissionStatType =
            when (value) {
                VIEWS.value -> VIEWS
                FILES_SIZE.value -> FILES_SIZE
                DIRECTORIES.value -> DIRECTORIES
                EMPTY_DIRECTORIES.value -> EMPTY_DIRECTORIES
                else -> throw IllegalArgumentException("Unknown SubmissionStatType '$value")
            }
    }
}
