package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRT
import java.time.Instant

interface NotificationsDataService {
    fun saveRtNotification(
        accNo: String,
        ticketId: String,
    ): SubmissionRT

    fun updateRtNotification(accNo: String): SubmissionRT

    fun findTicketId(accNo: String): String?
}
