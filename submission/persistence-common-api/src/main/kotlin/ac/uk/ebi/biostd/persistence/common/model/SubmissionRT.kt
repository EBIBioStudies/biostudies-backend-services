package ac.uk.ebi.biostd.persistence.common.model

import java.time.Instant

interface SubmissionRT {
    val accNo: String
    val ticketId: String
    val lastModified: Instant
}
