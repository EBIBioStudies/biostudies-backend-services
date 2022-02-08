package ac.uk.ebi.biostd.itest.test.submission.refresh

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.test.submission.refresh.SubmissionRefreshApiTestHelper.ACC_NO
import ac.uk.ebi.biostd.itest.test.submission.refresh.SubmissionRefreshApiTestHelper.ATTR_NAME
import ac.uk.ebi.biostd.itest.test.submission.refresh.SubmissionRefreshApiTestHelper.ATTR_VALUE
import ac.uk.ebi.biostd.itest.test.submission.refresh.SubmissionRefreshApiTestHelper.NEW_ATTR_FILE_FILELIST
import ac.uk.ebi.biostd.itest.test.submission.refresh.SubmissionRefreshApiTestHelper.NEW_ATTR_VALUE
import ac.uk.ebi.biostd.itest.test.submission.refresh.SubmissionRefreshApiTestHelper.NEW_SUBTITLE
import ac.uk.ebi.biostd.itest.test.submission.refresh.SubmissionRefreshApiTestHelper.SUBTITLE
import ac.uk.ebi.biostd.itest.test.submission.refresh.SubmissionRefreshApiTestHelper.TEST_FILE_NAME
import ac.uk.ebi.biostd.itest.test.submission.refresh.SubmissionRefreshApiTestHelper.assertExtSubmission
import ac.uk.ebi.biostd.itest.test.submission.refresh.SubmissionRefreshApiTestHelper.testSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DETAIL_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE_LIST_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import java.time.ZoneOffset.UTC

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionRefreshApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class, MongoDbReposConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SubmissionRefreshApiTest(
        @Autowired val mongoTemplate: MongoTemplate,
        @Autowired val tagsRefRepository: TagDataRepository,
        @Autowired val securityTestService: SecurityTestService,
        @Autowired val sequenceRepository: SequenceDataRepository,
        @Autowired val submissionRepository: SubmissionQueryService,
        @Autowired val fileListRepository: FileListDocFileDocDataRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0
        private lateinit var webClient: BioWebClient

        private val releaseDate = LocalDate.of(2017, 7, 4).atStartOfDay().atOffset(UTC)
        private val newReleaseDate = LocalDate.now(UTC).atStartOfDay().atOffset(UTC).plusDays(1)

        @BeforeAll
        fun beforeAll() {
            setUpTestData()
            setUpWebClient()
            setUpTestSubmission()
        }

        @Nested
        @EnabledIfSystemProperty(named = "itest.mode", matches = "mongo")
        inner class SubmissionRefreshMongoApiTest {
            @Test
            fun `refresh mongo submission release date and attributes`() {
                updateMongoSubmission()
                updateMongoFileList()
                webClient.refreshSubmission(ACC_NO)
                assertRefreshedSubmission()
                assertRefreshedFileList()
            }

            private fun assertRefreshedFileList() {
                TODO("Not yet implemented")
            }

            private fun updateMongoSubmission() {
                val query = Query(where(SUB_ACC_NO).`is`(ACC_NO).andOperator(where(SUB_VERSION).`is`(1)))
                val update = update(SUB_TITLE, NEW_SUBTITLE)
                    .set(SUB_RELEASE_TIME, newReleaseDate.toInstant())
                    .set(SUB_ATTRIBUTES, listOf(DocAttribute(ATTR_NAME, NEW_ATTR_VALUE)))
                mongoTemplate.updateFirst(query, update, DocSubmission::class.java)
            }

            private fun updateMongoFileList() {
                val query = Query(
                    where(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO).`is`(ACC_NO)
                        .andOperator(where(FILE_LIST_DOC_FILE_FILE_LIST_NAME).`is`("fileList.txt"))
                )
                val update = update(
                    "$FILE_LIST_DOC_FILE_FILE.$FILE_DOC_ATTRIBUTES[0].$ATTRIBUTE_DETAIL_VALUE",
                    NEW_ATTR_FILE_FILELIST
                )
                mongoTemplate.updateFirst(query, update, FileListDocFile::class.java)
            }
        }

        private fun setUpWebClient() {
            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

        private fun setUpTestData() {
            sequenceRepository.save(DbSequence("S-BSST"))
            tagsRefRepository.save(DbTag(classifier = "classifier", name = "tag"))
        }

        private fun setUpTestSubmission() {
            val refreshFile = tempFolder.createFile(TEST_FILE_NAME, "file content")
            webClient.submitSingle(testSubmission, TSV, listOf(refreshFile))

            assertExtSubmission(
                extSubmission = submissionRepository.getExtByAccNo(ACC_NO),
                title = SUBTITLE,
                releaseTime = releaseDate,
                attributes = listOf(ATTR_NAME to ATTR_VALUE)
            )
        }

        private fun assertRefreshedSubmission() =
            assertExtSubmission(
                extSubmission = submissionRepository.getExtByAccNo(ACC_NO),
                title = NEW_SUBTITLE,
                releaseTime = newReleaseDate,
                attributes = listOf(ATTR_NAME to NEW_ATTR_VALUE)
            )
    }
}
