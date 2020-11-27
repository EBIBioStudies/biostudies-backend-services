package ac.uk.ebi.biostd.itest.test.submission.refresh

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.DbSubmissionAttribute
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import arrow.core.Either
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.collections.ifLeft
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
internal class SubmissionRefreshApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SubmissionRefreshApiTest(
        @Autowired val securityTestService: SecurityTestService,
        @Autowired val submissionRepository: SubmissionQueryService,
        @Autowired val submissionDataRepository: SubmissionDataRepository,
        @Autowired val sequenceRepository: SequenceDataRepository,
        @Autowired val tagsRefRepository: TagDataRepository
    ) {
        private val releaseDate = LocalDate.of(2017, 7, 4).atStartOfDay().atOffset(ZoneOffset.UTC)
        private val newReleaseDate = LocalDate.now(ZoneOffset.UTC).atStartOfDay().atOffset(ZoneOffset.UTC).plusDays(1)

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
            assertExtSubmission(
                extSubmission = getExtSubmission(),
                title = SUBTITLE,
                releaseTime = releaseDate,
                accessTags = listOf(PUBLIC_ACCESS_TAG.value, "biostudies-mgmt@ebi.ac.uk"),
                attributes = listOf(ATTR_NAME to ATTR_VALUE))

            updateSubmission(getSubmissionDb())
            webClient.refreshSubmission(ACC_NO)

            val stored = getExtSubmission()
            assertExtSubmission(
                extSubmission = stored,
                title = NEW_SUBTITLE,
                releaseTime = newReleaseDate,
                accessTags = listOf("biostudies-mgmt@ebi.ac.uk"),
                attributes = listOf(ATTR_NAME to NEW_ATTR_VALUE))
        }

        private fun assertExtSubmission(
            extSubmission: ExtSubmission,
            title: String,
            releaseTime: OffsetDateTime,
            accessTags: List<String>,
            attributes: List<Pair<String, String>>
        ) {
            assertThat(extSubmission.title).isEqualTo(title)
            assertThat(extSubmission.releaseTime).isEqualTo(releaseTime)
            assertThat(extSubmission.rootPath).isEqualTo(ROOT_PATH)
            assertThat(extSubmission.accessTags.map { it.name }).containsExactlyInAnyOrderElementsOf(accessTags)
            assertThat(extSubmission.attributes.map { it.name to it.value }).containsExactlyElementsOf(attributes)

            assertThat(extSubmission.section.type).isEqualTo("Study")
            assertThat(extSubmission.section.files).hasSize(2)

            assertFile(extSubmission.section.files.first(), "regular")
            assertFile(extSubmission.section.files.last(), "duplicated")
        }

        private fun assertFile(file: Either<ExtFile, ExtFileTable>, type: String) {
            assertThat(file.isLeft()).isTrue()
            file.ifLeft {
                assertThat(it.fileName).isEqualTo("refresh-file.txt")
                assertThat(it.attributes).hasSize(1)
                assertThat(it.attributes.first().name).isEqualTo("type")
                assertThat(it.attributes.first().value).isEqualTo(type)
            }
        }

        private fun updateSubmission(submission: DbSubmission) {
            submission.releaseTime = newReleaseDate
            submission.title = NEW_SUBTITLE
            submission.attributes = sortedSetOf(DbSubmissionAttribute(ATTR_NAME, NEW_ATTR_VALUE, 1))
            submissionDataRepository.save(submission)
        }

        private fun getExtSubmission(): ExtSubmission = submissionRepository.getExtByAccNo(ACC_NO)

        private fun getSubmissionDb(): DbSubmission = submissionDataRepository.findBasicWithAttributes(ACC_NO)!!
    }
}
