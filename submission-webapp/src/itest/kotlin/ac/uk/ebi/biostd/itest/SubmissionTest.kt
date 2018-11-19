package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.config.PersistenceConfig
import ac.uk.ebi.biostd.config.SubmitterConfig
import ac.uk.ebi.biostd.itest.common.setAppProperty
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.SubFields
import ebi.ac.uk.model.extensions.title
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(TemporaryFolderExtension::class)
@TestInstance(PER_CLASS)
class SubmissionTest(private val temporaryFolder: TemporaryFolder) {

    @BeforeAll
    fun init() {
        setAppProperty("{BASE_PATH}", temporaryFolder.root.absolutePath)
    }

    @Nested
    @ExtendWith(SpringExtension::class)
    @Import(value = [SubmitterConfig::class, PersistenceConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    inner class SimpleSubmission {

        @Autowired
        private lateinit var restTemplate: TestRestTemplate

        @Autowired
        private lateinit var submissionRepository: SubmissionRepository

        @Test
        fun `submit simple submission`() {
            val accNo = "SimpleAcc1"
            val title = "Simple Submission"
            val submission = Submission(accNo = accNo)
            submission[SubFields.TITLE] = title

            // TODO add client instead
            val response = restTemplate.postForEntity("/submissions", submission, Submission::class.java)
            assertThat(response).isNotNull

            val savedSubmission = submissionRepository.findByAccNo(accNo)
            assertThat(savedSubmission).isNotNull
            assertThat(savedSubmission.accNo).isEqualTo(accNo)
            assertThat(savedSubmission.title).isEqualTo(title)
        }
    }
}
