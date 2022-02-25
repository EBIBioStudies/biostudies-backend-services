package ac.uk.ebi.biostd.itest.test.submission.refresh

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.common.BaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
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
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.model.DbSequence
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import arrow.core.Either
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.ifLeft
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import org.assertj.core.api.Assertions.assertThat
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

@ExtendWith(TemporaryFolderExtension::class)
internal class SubmissionRefreshApiTest(private val tempFolder: TemporaryFolder) : BaseIntegrationTest(tempFolder) {
    @Nested
    @Import(PersistenceConfig::class, MongoDbReposConfig::class)
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
        @Autowired val fileListRepository: FileListDocFileDocDataRepository,
    ) {
        @LocalServerPort
        private var serverPort: Int = 0
        private lateinit var webClient: BioWebClient

        private val releaseDate = LocalDate.of(2017, 7, 4).atStartOfDay().atOffset(UTC)
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
                    listOf(File(
                        "$FILE_LIST_FILE_NAME.txt",
                        attributes = listOf(Attribute(FILE_ATTR_NAME, FILE_ATTR_VALUE))
                    ))
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

        @Test
        fun `refresh mongo submission release date and attributes`() {
            webClient.submitSingle(testSubmission, TSV, listOf(refreshFile, fileList, fileListFile))
            assertExtSubmission(
                extSubmission = submissionRepository.getExtByAccNo(ACC_NO),
                title = SUBTITLE,
                releaseTime = releaseDate,
                attributes = listOf(ATTR_NAME to ATTR_VALUE)
            )
            assertFileListDocFileAttribute(subVersion = 1, attribute = DocAttribute(FILE_ATTR_NAME, FILE_ATTR_VALUE))

            updateMongoSubmission()
            updateMongoFileList()

            webClient.refreshSubmission(ACC_NO)

            assertExtSubmission(
                extSubmission = submissionRepository.getExtByAccNo(ACC_NO),
                title = NEW_SUBTITLE,
                releaseTime = newReleaseDate,
                attributes = listOf(ATTR_NAME to NEW_ATTR_VALUE)
            )
            assertFileListDocFileAttribute(subVersion = 2,
                attribute = DocAttribute(FILE_ATTR_NAME, FILE_NEW_ATTR_VALUE))
        }

        private fun updateMongoSubmission() {
            val query = Query(where(SUB_ACC_NO).`is`(ACC_NO).andOperator(where(SUB_VERSION).`is`(1)))
            val update = update(SUB_TITLE, NEW_SUBTITLE)
                .set(SUB_RELEASE_TIME, newReleaseDate.toInstant())
                .set(SUB_ATTRIBUTES, listOf(DocAttribute(ATTR_NAME, NEW_ATTR_VALUE)))
            mongoTemplate.updateFirst(query, update, DocSubmission::class.java)
        }

        private fun updateMongoFileList() {
            val docSubmission = mongoTemplate.find(
                Query(where(SUB_ACC_NO).`is`(ACC_NO).andOperator(where(SUB_VERSION).`is`(1))),
                DocSubmission::class.java
            ).first()

            val query = Query(where(FILE_LIST_DOC_FILE_SUBMISSION_ID).`is`(docSubmission.id)
                .andOperator(where(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO).`is`(ACC_NO)
                    .andOperator(where(FILE_LIST_DOC_FILE_SUBMISSION_VERSION).`is`(1)))
            )
            val update = update(
                "$FILE_LIST_DOC_FILE_FILE.$FILE_DOC_ATTRIBUTES",
                listOf(DocAttribute(FILE_ATTR_NAME, FILE_NEW_ATTR_VALUE))
            )
            mongoTemplate.updateFirst(query, update, FileListDocFile::class.java)
        }

        private fun assertFileListDocFileAttribute(subVersion: Int, attribute: DocAttribute) {
            val files = fileListRepository
                .findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
                    ACC_NO,
                    subVersion,
                    "$FILE_LIST_NAME.pagetab"
                )
            assertThat(files).hasSize(1)
            assertThat(files.first().file.attributes.first()).isEqualTo(attribute)
        }

        fun assertExtSubmission(
            extSubmission: ExtSubmission,
            title: String,
            releaseTime: OffsetDateTime,
            attributes: List<Pair<String, String>>,
        ) {
            assertThat(extSubmission.title).isEqualTo(title)
            assertThat(extSubmission.releaseTime).isEqualTo(releaseTime)
            assertThat(extSubmission.rootPath).isEqualTo(ROOT_PATH)
            assertThat(extSubmission.attributes.map { it.name to it.value }).containsExactlyElementsOf(attributes)

            assertThat(extSubmission.section.type).isEqualTo("Study")
            assertThat(extSubmission.section.files).hasSize(2)

            assertFile(extSubmission.section.files.first(), "regular")
            assertFile(extSubmission.section.files.last(), "duplicated")
        }

        private fun assertFile(file: Either<ExtFile, ExtFileTable>, type: String) {
            assertThat(file.isLeft()).isTrue
            file.ifLeft {
                assertThat(it.fileName).isEqualTo(TEST_FILE_NAME)
                assertThat(it.attributes).hasSize(1)
                assertThat(it.attributes.first().name).isEqualTo("type")
                assertThat(it.attributes.first().value).isEqualTo(type)
            }
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
