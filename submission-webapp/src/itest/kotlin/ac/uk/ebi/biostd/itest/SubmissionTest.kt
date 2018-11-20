package ac.uk.ebi.biostd.itest

import ac.uk.ebi.biostd.config.PersistenceConfig
import ac.uk.ebi.biostd.config.SubmitterConfig
import ac.uk.ebi.biostd.itest.common.setAppProperty
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.SubFields
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.security.integration.model.SignUpRequest
import ebi.ac.uk.security.service.SecurityService
import ebi.ac.uk.security.web.HEADER_NAME
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import net.soundvibe.jkob.json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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

        @Autowired
        private lateinit var userRepository: UserDataRepository

        @Autowired
        private lateinit var securityService: SecurityService

        private lateinit var token: String

        @BeforeEach
        fun init() {
            //TODO: teardown properly
            securityService.registerUser(SignUpRequest("test@biostudies.com", "jhon_doe", "12345"))
            token = securityService.login("jhon_doe", "12345")
        }

        @Test
        fun `submit simple submission`() {
            val accNo = "SimpleAcc1"
            val title = "Simple Submission"
            val submission = Submission(accNo = accNo)
            submission[SubFields.TITLE] = title

            // TODO add client instead
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.accept = listOf(MediaType.APPLICATION_JSON)
            headers.set(HEADER_NAME, token)
            headers.set(ACCEPT, MediaType.APPLICATION_JSON.toString())

            val submis = json {
                "accNo" to accNo
                "attributes"[{
                    "name" to "Title"
                    "value" to title
                }]
            }.toString()

            // TODO: add serializer as http message converters in rest end point
            val response: ResponseEntity<String> = restTemplate.exchange(
                "/submissions",
                HttpMethod.POST,
                HttpEntity(submis, headers),
                String::class.java)

            assertThat(response).isNotNull
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

            val savedSubmission = submissionRepository.findByAccNo(accNo)
            assertThat(savedSubmission).isNotNull
            assertThat(savedSubmission.accNo).isEqualTo(accNo)
            assertThat(savedSubmission.title).isEqualTo(title)
        }
    }
}
