package ac.uk.ebi.biostd.persistence.projections

import ac.uk.ebi.biostd.persistence.model.SIMPLE_QUERY_GRAPH
import ac.uk.ebi.biostd.persistence.model.ext.title
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus
import java.time.OffsetDateTime
import ac.uk.ebi.biostd.persistence.model.Submission as SubmissionDb

/**
 * Submission simple projection. Contains only submission attributes (no related entities).
 */
data class SimpleSubmission(
    val accNo: String,
    val relPath: String,
    val released: Boolean,
    val secretKey: String,
    val title: String?,
    val version: Int,
    val releaseTime: OffsetDateTime?,
    val modificationTime: OffsetDateTime,
    val creationTime: OffsetDateTime,
    val method: SubmissionMethod?,
    var status: ProcessingStatus
) {

    companion object {
        const val SIMPLE_GRAPH: String = SIMPLE_QUERY_GRAPH

        fun SubmissionDb.asSimpleSubmission(): SimpleSubmission {
            val title = if (title.isNullOrBlank()) rootSection.title else title

            return SimpleSubmission(
                accNo = accNo,
                version = version,
                secretKey = secretKey,
                title = title,
                relPath = relPath,
                released = released,
                creationTime = creationTime,
                modificationTime = modificationTime,
                releaseTime = releaseTime,
                status = status,
                method = method
            )
        }
    }
}
