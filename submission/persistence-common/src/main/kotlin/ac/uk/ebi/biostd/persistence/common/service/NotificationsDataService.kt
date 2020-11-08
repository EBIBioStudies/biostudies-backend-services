package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRT

interface NotificationsDataService {

    fun saveRtNotification(accNo: String, ticketId: String): SubmissionRT

    fun findTicketId(accNo: String): String?
}
