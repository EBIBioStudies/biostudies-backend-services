package ac.uk.ebi.biostd.persistence.common.model

enum class SubmissionStatType(val value: String) {
    VIEWS("VIEWS"),
    FILES_SIZE("FILES_SIZE");

    companion object {
        fun fromString(value: String): SubmissionStatType = when(value) {
            VIEWS.value -> VIEWS
            FILES_SIZE.value -> FILES_SIZE
            else -> throw IllegalArgumentException("Unknown SubmissionStatType '$value")
        }
    }
}
