package ac.uk.ebi.biostd.itest.test.submission.refresh

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.DbSubmissionAttribute
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.Sequence
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ebi.ac.uk.dsl.attribute
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

private const val accNo = "SimpleAcc1"
private const val subRootPath = "test-RootPath"
private const val subTitle = "Simple Submission"
private const val attr = "custom-attribute"
private const val attrVal = "custom-attribute-value"
private const val releaseDateString = "2017-07-04"
private val releaseDate = LocalDate.of(2017, 7, 4).atStartOfDay().atOffset(ZoneOffset.UTC)

private const val newSubTitle = "Simple Submission"
private const val newAttrVal = "custom-attribute-new-value"
private val newReleaseDate = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC).plusDays(1)

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionApiTest(tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SubmissionApiTest(
        @Autowired val submissionRepository: SubmissionDataRepository,
        @Autowired val sequenceRepository: SequenceDataRepository,
        @Autowired val tagsRefRepository: TagDataRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient

        @BeforeAll
        fun init() {
            webClient = getWebClient(serverPort, SuperUser)

            sequenceRepository.save(Sequence("S-BSST"))
            tagsRefRepository.save(DbTag(classifier = "classifier", name = "tag"))
        }

        @Test
        fun `refresh object when becoming public and changed attributed`() {
            val submission = submission(accNo) {
                title = subTitle
                releaseDate = releaseDateString
                rootPath = subRootPath
                attribute(attr, attrVal)
            }

            webClient.submitSingle(submission, TSV)
            val dbSubmission = getSubmissionDb(accNo)
            assertSubmission(
                submission = getSubmissionDb(accNo),
                title = subTitle,
                releaseTime = releaseDate,
                rootPath = subRootPath,
                accessTags = listOf(PUBLIC_ACCESS_TAG.value),
                attributes = listOf(attr to attrVal))

            updateSubmission(dbSubmission)
            webClient.refreshSubmission(accNo)

            val stored = getSubmissionDb(accNo)
            assertSubmission(
                submission = stored,
                title = newSubTitle,
                releaseTime = newReleaseDate,
                rootPath = subRootPath,
                accessTags = emptyList(),
                attributes = listOf(attr to newAttrVal))
        }

        private fun assertSubmission(
            submission: DbSubmission,
            title: String,
            releaseTime: OffsetDateTime,
            rootPath: String,
            accessTags: List<String>,
            attributes: List<Pair<String, String>>
        ) {
            assertThat(submission.title).isEqualTo(title)
            assertThat(submission.releaseTime).isEqualTo(releaseTime)
            assertThat(submission.rootPath).isEqualTo(rootPath)
            assertThat(submission.accessTags.map { it.name }).containsExactlyElementsOf(accessTags)
            assertThat(submission.attributes.map { it.name to it.value }).containsExactlyElementsOf(attributes)
        }

        private fun updateSubmission(submission: DbSubmission) {
            submission.releaseTime = newReleaseDate
            submission.title = newSubTitle
            submission.attributes = sortedSetOf(DbSubmissionAttribute(attr, newAttrVal, 1))
            submissionRepository.save(submission)
        }

        private fun getSubmissionDb(accNo: String): DbSubmission =
            submissionRepository.getByAccNoAndVersionGreaterThan(accNo, 0)!!
    }
}
