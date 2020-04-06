package ebi.ac.uk.notifications.persistence.repositories

import ebi.ac.uk.notifications.persistence.model.SubmissionRT
import org.springframework.data.jpa.repository.JpaRepository

interface SubmissionRtRepository : JpaRepository<SubmissionRT, Long>
