package ebi.ac.uk.notifications.persistence.repositories

import ebi.ac.uk.notifications.persistence.model.SubmissionRt
import org.springframework.data.jpa.repository.JpaRepository

interface SubmissionRtRepository : JpaRepository<SubmissionRt, Long>
