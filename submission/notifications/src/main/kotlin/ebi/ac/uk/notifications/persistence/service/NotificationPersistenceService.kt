package ebi.ac.uk.notifications.persistence.service

import ebi.ac.uk.notifications.persistence.model.SubmissionRT
import ebi.ac.uk.notifications.persistence.repositories.SubmissionRtRepository

class NotificationPersistenceService(private val submissionRtRepository: SubmissionRtRepository) {
    fun saveRtNotification(accNo: String, ticketId: String) = submissionRtRepository.save(SubmissionRT(accNo, ticketId))
}
