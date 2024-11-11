package ac.uk.ebi.biostd.persistence.common.model

enum class SubmissionStatType(
    val value: String,
) {
    VIEWS("VIEWS"),
    FILES_SIZE("FILES_SIZE"),
    DIRECTORIES("DIRECTORIES"),
    ;

    companion object {
        fun fromString(value: String): SubmissionStatType =
            when (value) {
                VIEWS.value -> VIEWS
                FILES_SIZE.value -> FILES_SIZE
                DIRECTORIES.value -> DIRECTORIES
                else -> throw IllegalArgumentException("Unknown SubmissionStatType '$value")
            }
    }
}
