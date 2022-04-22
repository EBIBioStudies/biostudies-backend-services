package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.config.PersistenceConfig
import ac.uk.ebi.biostd.itest.assertions.AllInOneSubmissionHelper
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.common.clean
import ac.uk.ebi.biostd.itest.common.enableFire
import ac.uk.ebi.biostd.itest.common.getWebClient
import ac.uk.ebi.biostd.itest.common.mongoMode
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.factory.submissionSpecJson
import ac.uk.ebi.biostd.itest.factory.submissionSpecTsv
import ac.uk.ebi.biostd.itest.factory.submissionSpecXml
import ac.uk.ebi.biostd.itest.listener.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.listener.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSectionMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtSubmissionMethod.FILE
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@Import(PersistenceConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class AllInOneMultipartFileSubmissionTest(
    @Autowired var securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionQueryService,
    @LocalServerPort val serverPort: Int
) {
    private lateinit var webClient: BioWebClient
    private lateinit var allInOneSubmissionHelper: AllInOneSubmissionHelper
    private val toSubmissionMapper = ToSubmissionMapper(ToSectionMapper(ToFileListMapper()))

    @BeforeAll
    fun init() {
        tempFolder.clean()

        securityTestService.ensureRegisterUser(SuperUser)
        webClient = getWebClient(serverPort, SuperUser)
        allInOneSubmissionHelper = AllInOneSubmissionHelper(submissionPath, submissionRepository, toSubmissionMapper)
    }

    @Test
    fun `submit all in one multipart TSV submission`() {
        val (submission, fileList, files, subFileList) = submissionSpecTsv(tempFolder, "S-EPMC124")
        webClient.uploadFile(fileList)
        subFileList?.let { webClient.uploadFile(it.file, it.folder) }
        files.forEach { webClient.uploadFile(it.file, it.folder) }

        webClient.submitSingle(submission, emptyList())

        allInOneSubmissionHelper.assertSavedSubmission("S-EPMC124", method = FILE)
        if (mongoMode)
            if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC124")
            else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC124")
    }

    @Test
    fun `submit all in one multipart Json submission`() {
        val (submission, fileList, files, subFileList) = submissionSpecJson(tempFolder, "S-EPMC125")
        webClient.uploadFile(fileList)
        subFileList?.let { webClient.uploadFile(it.file, it.folder) }
        files.forEach { webClient.uploadFile(it.file, it.folder) }

        webClient.submitSingle(submission, emptyList())

        allInOneSubmissionHelper.assertSavedSubmission("S-EPMC125", method = FILE)
        if (mongoMode)
            if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC125")
            else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC125")
    }

    @Test
    fun `submit all in one multipart XML submission`() {
        val (submission, fileList, files, subFileList) = submissionSpecXml(tempFolder, "S-EPMC126")
        webClient.uploadFile(fileList)
        subFileList?.let { webClient.uploadFile(it.file, it.folder) }
        files.forEach { webClient.uploadFile(it.file, it.folder) }

        webClient.submitSingle(submission, emptyList())

        allInOneSubmissionHelper.assertSavedSubmission("S-EPMC126", method = FILE)
        if (mongoMode)
            if (enableFire) allInOneSubmissionHelper.assertSubmissionFilesRecordsFire("S-EPMC126")
            else allInOneSubmissionHelper.assertSubmissionFilesRecordsNfs("S-EPMC126")
    }
}
