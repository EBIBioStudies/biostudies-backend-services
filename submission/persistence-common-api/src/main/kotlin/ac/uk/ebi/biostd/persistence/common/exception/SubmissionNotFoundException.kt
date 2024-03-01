package ac.uk.ebi.biostd.persistence.common.exception

class SubmissionNotFoundException private constructor(message: String) : RuntimeException(message) {
    companion object {
        fun notFound(accNo: String) = SubmissionNotFoundException("The submission '$accNo' was not found")

        fun notFoundByVersion(
            accNo: String,
            version: Int,
        ) = SubmissionNotFoundException("The submission '$accNo' with version '$version' was not found")
    }
}
