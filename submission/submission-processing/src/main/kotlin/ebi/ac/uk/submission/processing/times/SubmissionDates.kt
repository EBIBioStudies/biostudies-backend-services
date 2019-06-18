package ebi.ac.uk.submission.processing.times

import java.time.OffsetDateTime

data class SubmissionDates(
    val createTime: OffsetDateTime,
    val modificationTime: OffsetDateTime,
    val releaseDate: OffsetDateTime)
