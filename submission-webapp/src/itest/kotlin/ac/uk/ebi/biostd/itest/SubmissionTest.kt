package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.config.SubmitterConfig
import ac.uk.ebi.biostd.itest.common.setAppProperty
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.SubFields
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
    @Import(value = [SubmitterConfig::class])
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    class SimpleSubmission {

        @Autowired
        private lateinit var restTemplate: TestRestTemplate

        @Test
        fun `submit simple submission`() {
            val submission = Submission(accNo = "SimpleAcc1")
            submission[SubFields.TITLE] = "`submit simple submission`"

            // TODO add client instead
            // TODO validate submission was save in h2 db
            val response = restTemplate.postForEntity("/submissions", submission, Submission::class.java)
            assertThat(response).isNotNull
        }
    }
}
