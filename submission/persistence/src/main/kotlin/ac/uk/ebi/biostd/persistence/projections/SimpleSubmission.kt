package ac.uk.ebi.biostd.persistence.projections

import ac.uk.ebi.biostd.persistence.model.SIMPLE_QUERY_GRAPH
import ebi.ac.uk.functions.secondsToInstant
import ebi.ac.uk.model.constants.ProcessingStatus
import java.time.OffsetDateTime
import java.time.ZoneOffset
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
    val releaseTime: OffsetDateTime,
    val modificationTime: OffsetDateTime,
    val creationTime: OffsetDateTime,
    var status: ProcessingStatus
) {

    companion object {

        const val SIMPLE_GRAPH: String = SIMPLE_QUERY_GRAPH
        fun SubmissionDb.asSimpleSubmission(): SimpleSubmission {
            return SimpleSubmission(
                accNo = accNo,
                version = version,
                secretKey = secretKey,
                title = title,
                relPath = relPath,
                released = released,
                creationTime = toInstant(creationTime),
                modificationTime = toInstant(releaseTime),
                releaseTime = toInstant(releaseTime),
                status = status
            )
        }

        private fun toInstant(dateSeconds: Long) = secondsToInstant(dateSeconds).atOffset(ZoneOffset.UTC)
    }
}
