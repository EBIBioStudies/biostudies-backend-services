package ac.uk.ebi.biostd.itest.test.submission.refresh

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.createFileList
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.doc.MongoDbConfig
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
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
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.Update.update
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import java.time.ZoneOffset.UTC

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionRefreshApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(MongoDbConfig::class, PersistenceConfig::class, MongoDbReposConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    @EnabledIfSystemProperty(named = "itest.mode", matches = "mongo")
    inner class RefreshSubmissionTest(
        @Autowired val mongoTemplate: MongoTemplate,
        @Autowired val tagsRefRepository: TagDataRepository,
        @Autowired val securityTestService: SecurityTestService,
        @Autowired val sequenceRepository: SequenceDataRepository,
        @Autowired val submissionRepository: SubmissionQueryService,
        @Autowired val submissionDocRepository: SubmissionDocDataRepository,
        @Autowired val submissionRequestRepository: SubmissionRequestDocDataRepository,
        @Autowired val fileListRepository: FileListDocFileDocDataRepository
    ) {
        @LocalServerPort
        private var serverPort: Int = 0
        private lateinit var webClient: BioWebClient

        private val newReleaseDate = LocalDate.now(UTC).atStartOfDay().atOffset(UTC).plusDays(1)
        private val testSubmission = submission(ACC_NO) {
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
        val refreshFile = tempFolder.createFile(TEST_FILE_NAME, "file content")
        val fileList = tempFolder.createFile(
            "$FILE_LIST_NAME.pagetab.tsv",
            tsv {
                line("Files", FILE_ATTR_NAME)
                line("$FILE_LIST_FILE_NAME.txt", FILE_ATTR_VALUE)
            }.toString()
        )
        val fileListFile = tempFolder.createFile("$FILE_LIST_FILE_NAME.txt", "content fileList file")

        @BeforeAll
        fun init() {
            sequenceRepository.save(DbSequence("S-BSST"))
            tagsRefRepository.save(DbTag(classifier = "classifier", name = "tag"))

            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

        @BeforeEach
        fun beforeEach() {
            fileListRepository.deleteAll()
            submissionDocRepository.deleteAll()
            submissionRequestRepository.deleteAll()

            webClient.submitSingle(testSubmission, TSV, listOf(refreshFile, fileList, fileListFile))
        }

        @Test
        fun `refresh when submission title is updated`() {
            val query = Query(where(SUB_ACC_NO).`is`(ACC_NO).andOperator(where(SUB_VERSION).`is`(1)))
            val update = update(SUB_TITLE, NEW_SUBTITLE)
            mongoTemplate.updateFirst(query, update, DocSubmission::class.java)

            webClient.refreshSubmission(ACC_NO)

            val submission = submissionRepository.getExtByAccNo(ACC_NO)
            assertThat(submission.title).isEqualTo(NEW_SUBTITLE)
        }

        @Test
        fun `refresh when submission release date is updated`() {
            val query = Query(where(SUB_ACC_NO).`is`(ACC_NO).andOperator(where(SUB_VERSION).`is`(1)))
            val update = update(SUB_RELEASE_TIME, newReleaseDate.toInstant())
            mongoTemplate.updateFirst(query, update, DocSubmission::class.java)

            webClient.refreshSubmission(ACC_NO)

            val submission = submissionRepository.getExtByAccNo(ACC_NO)
            assertThat(submission.releaseTime).isEqualTo(newReleaseDate)
        }

        @Test
        fun `refresh when submission attribute is updated`() {
            val query = Query(where(SUB_ACC_NO).`is`(ACC_NO).andOperator(where(SUB_VERSION).`is`(1)))
            val update = update(SUB_ATTRIBUTES, listOf(DocAttribute(ATTR_NAME, NEW_ATTR_VALUE)))
            mongoTemplate.updateFirst(query, update, DocSubmission::class.java)

            webClient.refreshSubmission(ACC_NO)

            val submission = submissionRepository.getExtByAccNo(ACC_NO)
            assertThat(submission.attributes).isEqualTo(listOf(ExtAttribute(ATTR_NAME, NEW_ATTR_VALUE)))
        }

        @Test
        fun `refresh when submission fileListFile attribute is updated`() {
            val docSubmission = mongoTemplate.findOne(
                Query(where(SUB_ACC_NO).`is`(ACC_NO).andOperator(where(SUB_VERSION).`is`(1))),
                DocSubmission::class.java
            )!!
            val query = Query(
                where(FILE_LIST_DOC_FILE_SUBMISSION_ID).`is`(docSubmission.id)
                    .andOperator(
                        where(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO).`is`(ACC_NO)
                            .andOperator(where(FILE_LIST_DOC_FILE_SUBMISSION_VERSION).`is`(1))
                    )
            )
            val update = update(
                "$FILE_LIST_DOC_FILE_FILE.$FILE_DOC_ATTRIBUTES",
                listOf(DocAttribute(FILE_ATTR_NAME, FILE_NEW_ATTR_VALUE))
            )
            mongoTemplate.updateFirst(query, update, FileListDocFile::class.java)

            webClient.refreshSubmission(ACC_NO)

            val files = fileListRepository
                .findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(ACC_NO, 1, "$FILE_LIST_NAME.pagetab")
            assertThat(files).hasSize(1)
            assertThat(files.first().file.attributes)
                .isEqualTo(listOf(DocAttribute(FILE_ATTR_NAME, FILE_NEW_ATTR_VALUE)))
        }

        @Test
        fun `refresh when file section is added`() {
            val sub = submissionRepository.getExtByAccNo(ACC_NO)
            assertThat(sub.section.files).hasSize(2)
            val file = tempFolder.createFile("addFile.txt", "file content")
            val docFile =
                NfsDocFile(
                    file.name,
                    file.path,
                    "relPath",
                    file.absolutePath,
                    listOf(),
                    file.md5(),
                    file.size(),
                    "file"
                )
            val query = Query(where(SUB_ACC_NO).`is`(ACC_NO).andOperator(where(SUB_VERSION).`is`(1)))
            val update = Update().push("$SUB_SECTION.$SEC_FILES", docFile)
            mongoTemplate.updateFirst(query, update, DocSubmission::class.java)

            webClient.refreshSubmission(ACC_NO)

            val updatedSub = submissionDocRepository.getSubmission(ACC_NO, 2)
            val files = updatedSub.section.files
            assertThat(files).hasSize(3)
            if (enableFire) assertThat(files.third()).hasLeftValueSatisfying {
                require(it is FireDocFile)
                assertThat(it.fileName).isEqualTo(file.name)
                assertThat(it.filePath).isEqualTo(file.path)
                assertThat(it.relPath).isEqualTo("relPath")
                assertThat(it.fireId).isNotNull()
                assertThat(it.md5).isEqualTo(file.md5())
                assertThat(it.fileSize).isEqualTo(file.size())
            }
            else assertThat(files.third()).hasLeftValueSatisfying { assertThat(it).isEqualTo(docFile) }
        }
    }

    private companion object {
        const val ROOT_PATH = "test-RootPath"
        const val RELEASE_DATE_STRING = "2017-07-04"
        const val ACC_NO = "SimpleAcc1"
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
