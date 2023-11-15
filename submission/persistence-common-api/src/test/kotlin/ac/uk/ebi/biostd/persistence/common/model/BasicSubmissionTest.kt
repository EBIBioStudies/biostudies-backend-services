package ac.uk.ebi.biostd.persistence.common.model

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ebi.ac.uk.model.SubmissionMethod.PAGE_TAB
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class BasicSubmissionTest {
    @Test
    fun `completion percentage`() {
        assertThat(basicSubmission(REQUESTED).completionPercentage).isEqualTo(0.09)
        assertThat(basicSubmission(INDEXED).completionPercentage).isEqualTo(0.23)
        assertThat(basicSubmission(LOADED).completionPercentage).isEqualTo(0.37)
        assertThat(basicSubmission(CLEANED).completionPercentage).isEqualTo(0.51)
        assertThat(basicSubmission(FILES_COPIED).completionPercentage).isEqualTo(0.66)
        assertThat(basicSubmission(CHECK_RELEASED).completionPercentage).isEqualTo(0.71)
        assertThat(basicSubmission(PERSISTED).completionPercentage).isEqualTo(0.86)
        assertThat(basicSubmission(PROCESSED).completionPercentage).isEqualTo(1.0)
    }

    private fun basicSubmission(status: RequestStatus) = BasicSubmission(
        accNo = ACC_NO,
        relPath = REL_PATH,
        released = false,
        secretKey = SECRET_KEY,
        title = TITLE,
        version = 1,
        releaseTime = OffsetDateTime.now(),
        modificationTime = OffsetDateTime.now(),
        creationTime = OffsetDateTime.now(),
        method = PAGE_TAB,
        status = status,
        totalFiles = TOTAL_FILES,
        currentIndex = CURRENT_INDEX,
        owner = OWNER
    )

    companion object {
        private const val ACC_NO = "S-BSST1"
        private const val REL_PATH = "S-BSST/001/S-BSST1"
        private const val TITLE = "Test Title"
        private const val OWNER = "owner@mail.org"
        private const val SECRET_KEY = "the-secret-key"
        private const val CURRENT_INDEX = 6
        private const val TOTAL_FILES = 10
    }
}
