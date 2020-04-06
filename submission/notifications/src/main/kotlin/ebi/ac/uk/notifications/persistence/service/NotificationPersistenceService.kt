package ebi.ac.uk.notifications.persistence.service

import ebi.ac.uk.notifications.persistence.model.SubmissionRt
import ebi.ac.uk.notifications.persistence.repositories.SubmissionRtRepository

class NotificationPersistenceService(private val submissionRtRepository: SubmissionRtRepository) {
    fun saveRtNotification(accNo: String, ticketId: String) = submissionRtRepository.save(SubmissionRt(accNo, ticketId))
}
