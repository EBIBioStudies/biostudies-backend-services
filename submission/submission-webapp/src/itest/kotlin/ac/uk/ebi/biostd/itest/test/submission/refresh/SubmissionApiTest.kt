package ac.uk.ebi.biostd.itest.test.submission.refresh

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.DbSubmissionAttribute
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

private const val ACC_NO = "SimpleAcc1"
private const val ROOT_PATH = "test-RootPath"
private const val SUBTITLE = "Simple Submission"
private const val ATTR_NAME = "custom-attribute"
private const val ATTR_VALUE = "custom-attribute-value"
private const val RELEASE_DATE_STRING = "2017-07-04"
private const val NEW_SUBTITLE = "Simple Submission"
private const val NEW_ATTR_VALUE = "custom-attribute-new-value"

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SubmissionApiTest(
        @Autowired val securityTestService: SecurityTestService,
        @Autowired val submissionRepository: SubmissionDataRepository,
        @Autowired val sequenceRepository: SequenceDataRepository,
        @Autowired val tagsRefRepository: TagDataRepository
    ) {
        private val releaseDate = LocalDate.of(2017, 7, 4).atStartOfDay().atOffset(ZoneOffset.UTC)
        private val newReleaseDate = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC).plusDays(1)

        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)

            sequenceRepository.save(Sequence("S-BSST"))
            tagsRefRepository.save(DbTag(classifier = "classifier", name = "tag"))
        }

        @Test
        fun `refresh object when becoming public and changed attributed`() {
            val refreshFile = tempFolder.createFile("refresh-file.txt")
            val submission = submission(ACC_NO) {
                title = SUBTITLE
                releaseDate = RELEASE_DATE_STRING
                rootPath = ROOT_PATH
                attribute(ATTR_NAME, ATTR_VALUE)

                section("Study") {
                    file("refresh-file.txt") {
                        attribute("type", "regular")
                    }

                    file("refresh-file.txt") {
                        attribute("type", "duplicated")
                    }
                }
            }

            webClient.submitSingle(submission, TSV, listOf(refreshFile))
            val dbSubmission = getSubmissionDb()
            assertSubmission(
                submission = getSubmissionDb(),
                title = SUBTITLE,
                releaseTime = releaseDate,
                accessTags = listOf(PUBLIC_ACCESS_TAG.value),
                attributes = listOf(ATTR_NAME to ATTR_VALUE))

            updateSubmission(dbSubmission)
            webClient.refreshSubmission(ACC_NO)

            val stored = getSubmissionDb()
            assertSubmission(
                submission = stored,
                title = NEW_SUBTITLE,
                releaseTime = newReleaseDate,
                accessTags = emptyList(),
                attributes = listOf(ATTR_NAME to NEW_ATTR_VALUE))
        }

        private fun assertSubmission(
            submission: DbSubmission,
            title: String,
            releaseTime: OffsetDateTime,
            accessTags: List<String>,
            attributes: List<Pair<String, String>>
        ) {
            assertThat(submission.title).isEqualTo(title)
            assertThat(submission.releaseTime).isEqualTo(releaseTime)
            assertThat(submission.rootPath).isEqualTo(ROOT_PATH)
            assertThat(submission.accessTags.map { it.name }).containsExactlyElementsOf(accessTags)
            assertThat(submission.attributes.map { it.name to it.value }).containsExactlyElementsOf(attributes)

            assertThat(submission.rootSection.type).isEqualTo("Study")
            assertThat(submission.rootSection.files).hasSize(2)

            assertFile(submission.rootSection.files.first(), "regular")
            assertFile(submission.rootSection.files.last(), "duplicated")
        }

        private fun assertFile(file: DbFile, type: String) {
            assertThat(file.name).isEqualTo("refresh-file.txt")
            assertThat(file.attributes).hasSize(1)
            assertThat(file.attributes.first().name).isEqualTo("type")
            assertThat(file.attributes.first().value).isEqualTo(type)
        }

        private fun updateSubmission(submission: DbSubmission) {
            submission.releaseTime = newReleaseDate
            submission.title = NEW_SUBTITLE
            submission.attributes = sortedSetOf(DbSubmissionAttribute(ATTR_NAME, NEW_ATTR_VALUE, 1))
            submissionRepository.save(submission)
        }

        private fun getSubmissionDb(): DbSubmission = submissionRepository.readByAccNoAndVersionGreaterThan(ACC_NO, 0)!!
    }
}
