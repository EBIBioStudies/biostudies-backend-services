package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.createFileList
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update.update
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmissionRefreshApiTest(
    @Autowired val mongoTemplate: ReactiveMongoTemplate,
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val fileListRepository: FileListDocFileDocDataRepository,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    private val newReleaseDate = LocalDate.now(UTC).atStartOfDay().atOffset(UTC).plusDays(1)
    private val refreshFile = tempFolder.createFile(TEST_FILE_NAME, "file content")
    private val fileList = tempFolder.createFile(
        "$FILE_LIST_NAME.tsv",
        tsv {
            line("Files", FILE_ATTR_NAME)
            line("$FILE_LIST_FILE_NAME.txt", FILE_ATTR_VALUE)
        }.toString()
    )
    private val fileListFile = tempFolder.createFile("$FILE_LIST_FILE_NAME.txt", "content fileList file")

    @BeforeAll
    fun init() = runBlocking {
        securityTestService.ensureUserRegistration(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)
    }

    private fun createTestSubmission(accNo: String) {
        fun testSubmission(accNo: String): Submission {
            return submission(accNo) {
                title = SUBTITLE
                releaseDate = OffsetDateTime.now().toStringDate()
                rootPath = ROOT_PATH
                attribute(ATTR_NAME, ATTR_VALUE)

                section("Study") {
                    file("refresh-file.txt") {
                        attribute("type", "regular")
                    }

                    file("refresh-file.txt") {
                        attribute("type", "duplicated")
                    }

                    fileList = FileList(
                        FILE_LIST_NAME,
                        createFileList(
                            BioFile(
                                "$FILE_LIST_FILE_NAME.txt",
                                attributes = listOf(Attribute(FILE_ATTR_NAME, FILE_ATTR_VALUE))
                            )
                        )
                    )
                }
            }
        }

        val filesConfig = SubmissionFilesConfig(
            storageMode = storageMode,
            files = listOf(refreshFile, fileList, fileListFile)
        )
        val testSubmission = testSubmission(accNo)
        webClient.submitSingle(testSubmission, TSV, filesConfig)
    }

    @Test
    fun `25-1 refresh when submission title is updated`() = runTest {
        val accNo = "Refresh-title-001"
        createTestSubmission(accNo)

        val query = Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).gt(0)))
        val update = update(SUB_TITLE, NEW_SUBTITLE)
        mongoTemplate.updateFirst(query, update, DocSubmission::class.java).awaitSingleOrNull()

        webClient.refreshSubmission(accNo)

        val submission = submissionRepository.getExtByAccNo(accNo)
        assertThat(submission.title).isEqualTo(NEW_SUBTITLE)
    }

    @Test
    fun `25-2 refresh when submission release date is updated`() = runTest {
        val accNo = "Refresh-release-001"
        createTestSubmission(accNo)

        val query = Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).gt(0)))
        val update = update(SUB_RELEASE_TIME, newReleaseDate.toInstant())
        mongoTemplate.updateFirst(query, update, DocSubmission::class.java).awaitSingleOrNull()

        webClient.refreshSubmission(accNo)

        val submission = submissionRepository.getExtByAccNo(accNo)
        assertThat(submission.releaseTime).isEqualTo(newReleaseDate)
    }

    @Test
    fun `25-3 refresh when submission attribute is updated`() = runTest {
        val accNo = "Refresh-attribute-001"
        createTestSubmission(accNo)

        val query = Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).gt(0)))
        val update = update(SUB_ATTRIBUTES, listOf(DocAttribute(ATTR_NAME, NEW_ATTR_VALUE)))
        mongoTemplate.updateFirst(query, update, DocSubmission::class.java).awaitSingleOrNull()

        webClient.refreshSubmission(accNo)

        val submission = submissionRepository.getExtByAccNo(accNo)
        assertThat(submission.attributes).isEqualTo(listOf(ExtAttribute(ATTR_NAME, NEW_ATTR_VALUE)))
    }

    @Test
    fun `25-4 refresh when submission fileListFile attribute is updated`() = runTest {
        val accNo = "Refresh-fileList-attribute-001"
        createTestSubmission(accNo)

        val docSubmission = mongoTemplate.findOne(
            Query(where(SUB_ACC_NO).`is`(accNo).andOperator(where(SUB_VERSION).gt(0))),
            DocSubmission::class.java
        ).awaitSingle()
        val query = Query(
            where(FILE_LIST_DOC_FILE_SUBMISSION_ID).`is`(docSubmission.id)
                .andOperator(
                    where(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO).`is`(accNo)
                        .andOperator(where(FILE_LIST_DOC_FILE_SUBMISSION_VERSION).gt(0))
                )
        )
        val update = update(
            "$FILE_LIST_DOC_FILE_FILE.$FILE_DOC_ATTRIBUTES",
            listOf(DocAttribute(FILE_ATTR_NAME, FILE_NEW_ATTR_VALUE))
        )
        mongoTemplate.updateFirst(query, update, FileListDocFile::class.java).awaitSingleOrNull()

        webClient.refreshSubmission(accNo)

        val files = fileListRepository
            .findByFileList(accNo, 1, FILE_LIST_NAME)
            .toList()

        assertThat(files).hasSize(1)
        assertThat(files.first().file.attributes)
            .isEqualTo(listOf(DocAttribute(FILE_ATTR_NAME, FILE_NEW_ATTR_VALUE)))
    }

    private companion object {
        const val ROOT_PATH = "test-RootPath"
        const val SUBTITLE = "Simple Submission"
        const val ATTR_NAME = "custom-attribute"
        const val ATTR_VALUE = "custom-attribute-value"
        const val FILE_ATTR_NAME = "GEN"
        const val FILE_ATTR_VALUE = "ABC"
        const val FILE_NEW_ATTR_VALUE = "DEFG"
        const val NEW_SUBTITLE = "New Simple Submission"
        const val NEW_ATTR_VALUE = "custom-attribute-new-value"
        const val TEST_FILE_NAME = "refresh-file.txt"
        const val FILE_LIST_NAME = "fileListName"
        const val FILE_LIST_FILE_NAME = "fileListFileName"
    }
}
