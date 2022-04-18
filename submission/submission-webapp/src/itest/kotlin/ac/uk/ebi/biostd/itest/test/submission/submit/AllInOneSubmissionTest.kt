package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.XML
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.assertions.AllInOneSubmissionHelper
import ac.uk.ebi.biostd.itest.common.DummyBaseIntegrationTest
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.factory.submissionSpecJson
import ac.uk.ebi.biostd.itest.factory.submissionSpecTsv
import ac.uk.ebi.biostd.itest.factory.submissionSpecXml
import ac.uk.ebi.biostd.itest.listener.ITestListener
import ac.uk.ebi.biostd.itest.listener.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.listener.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSectionMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import java.io.File
import org.junit.jupiter.api.AfterAll
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
import org.springframework.transaction.annotation.Transactional

internal class AllInOneSubmissionTest : DummyBaseIntegrationTest() {
    @Nested
    @Import(PersistenceConfig::class)
    @ExtendWith(SpringExtension::class)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @DirtiesContext
    inner class SingleSubmissionTest(
        @Autowired val submissionRepository: SubmissionQueryService,
        @Autowired val securityTestService: SecurityTestService
    ) {
        @LocalServerPort
        private var serverPort: Int = 0

        private lateinit var webClient: BioWebClient
        private lateinit var allInOneSubmissionHelper: AllInOneSubmissionHelper
        private val toSubmissionMapper = ToSubmissionMapper(ToSectionMapper(ToFileListMapper()))

        @BeforeAll
        fun init() {
            val remainingDirectories = setOf("submission", "request-files", "dropbox", "magic", "tmp")
            tempFolder.listFiles()?.forEach {
                if (it.isFile) {
                    it.delete()
                } else {
                    if (it.name in remainingDirectories) it.cleanDirectory() else it.deleteRecursively()
                }
            }
            securityTestService.deleteSuperUser()

            securityTestService.registerUser(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
            allInOneSubmissionHelper = AllInOneSubmissionHelper(submissionPath, submissionRepository, toSubmissionMapper)
        }
        private fun File.cleanDirectory(): File {
            listFiles()?.forEach { it.deleteRecursively() }
            return this
        }

        @Test
        fun `submit all in one TSV submission`() {
            val (submission, fileList, files, subFileList) = submissionSpecTsv(tempFolder, "S-EPMC124")
            webClient.uploadFile(fileList)
            subFileList?.let { webClient.uploadFile(it.file, it.folder) }
            files.forEach { webClient.uploadFile(it.file, it.folder) }

            webClient.submitSingle(submission.readText(), TSV)

            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC124")
            if (mongoMode)
                if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC124")
                else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC124")
        }

        @Test
        fun `submit all in one Json submission`() {
            val (submission, fileList, files, subFileList) = submissionSpecJson(tempFolder, "S-EPMC125")
            webClient.uploadFile(fileList)
            subFileList?.let { webClient.uploadFile(it.file, it.folder) }
            files.forEach { webClient.uploadFile(it.file, it.folder) }

            webClient.submitSingle(submission.readText(), JSON)

            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC125")
            if (mongoMode)
                if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC125")
                else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC125")
        }

        @Test
        fun `submit all in one XML submission`() {
            val (submission, fileList, files, subFileList) = submissionSpecXml(tempFolder, "S-EPMC126")
            webClient.uploadFile(fileList)
            subFileList?.let { webClient.uploadFile(it.file, it.folder) }
            files.forEach { webClient.uploadFile(it.file, it.folder) }

            webClient.submitSingle(submission.readText(), XML)

            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC126")
            if (mongoMode)
                if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC126")
                else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC126")
        }
    }
}
