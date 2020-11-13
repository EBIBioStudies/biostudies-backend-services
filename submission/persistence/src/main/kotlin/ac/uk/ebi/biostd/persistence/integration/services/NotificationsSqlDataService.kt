package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRT
import ac.uk.ebi.biostd.persistence.common.service.NotificationsDataService
import ac.uk.ebi.biostd.persistence.model.DbSubmissionRT
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRtRepository

internal class NotificationsSqlDataService(
    private val submissionRtRepository: SubmissionRtRepository
) : NotificationsDataService {
    override fun saveRtNotification(accNo: String, ticketId: String): SubmissionRT =
        submissionRtRepository.save(DbSubmissionRT(accNo, ticketId))

    override fun findTicketId(accNo: String): String? = submissionRtRepository.findByAccNo(accNo)?.ticketId
}
