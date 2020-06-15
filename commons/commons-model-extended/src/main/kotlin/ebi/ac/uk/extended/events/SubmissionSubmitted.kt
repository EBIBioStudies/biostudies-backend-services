package ebi.ac.uk.extended.events

data class SubmissionSubmitted(
    val accNo: String,
    val pagetabUrl: String,
    val extTabUrl: String
) : java.io.Serializable
