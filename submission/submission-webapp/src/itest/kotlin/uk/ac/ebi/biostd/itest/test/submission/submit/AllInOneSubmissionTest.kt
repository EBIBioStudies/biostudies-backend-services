package uk.ac.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.submitAndWait
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.ac.ebi.biostd.itest.common.SecurityTestService
import uk.ac.ebi.biostd.itest.entities.SuperUser
import uk.ac.ebi.biostd.itest.factory.submissionSpecJson
import uk.ac.ebi.biostd.itest.factory.submissionSpecTsv
import uk.ac.ebi.biostd.itest.itest.ITestListener.Companion.enableFire
import uk.ac.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import uk.ac.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import uk.ac.ebi.biostd.itest.itest.getWebClient
import uk.ac.ebi.biostd.itest.util.AllInOneSubmissionHelper

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AllInOneSubmissionTest(
    @param:Autowired val subRepository: SubmissionPersistenceQueryService,
    @param:Autowired val securityTestService: SecurityTestService,
    @param:Autowired val toSubmissionMapper: ToSubmissionMapper,
    @param:LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient
    private lateinit var allInOneSubmissionHelper: AllInOneSubmissionHelper

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
            allInOneSubmissionHelper = AllInOneSubmissionHelper(submissionPath, subRepository, toSubmissionMapper)
        }

    @Test
    fun `2-1 Submit all in one TSV study`() =
        runTest {
            val (submission, fileList, files, subFileList) = submissionSpecTsv(tempFolder, "S-EPMC124")
            webClient.uploadFile(fileList)
            subFileList?.let { webClient.uploadFile(it.file, it.folder) }
            files.forEach { webClient.uploadFile(it.file, it.folder) }

            webClient.submitAndWait(submission.readText(), TSV)

            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC124")
            if (enableFire) {
                allInOneSubmissionHelper.assertFirePagetabFiles("S-EPMC124")
            } else {
                allInOneSubmissionHelper.assertNfsPagetabFiles("S-EPMC124")
            }
        }

    @Test
    fun `2-2 Submit all in one JSON study`() =
        runTest {
            val (submission, fileList, files, subFileList) = submissionSpecJson(tempFolder, "S-EPMC125")
            webClient.uploadFile(fileList)
            subFileList?.let { webClient.uploadFile(it.file, it.folder) }
            files.forEach { webClient.uploadFile(it.file, it.folder) }

            webClient.submitAndWait(submission.readText(), JSON)

            allInOneSubmissionHelper.assertSavedSubmission("S-EPMC125")
            if (enableFire) {
                allInOneSubmissionHelper.assertFirePagetabFiles("S-EPMC125")
            } else {
                allInOneSubmissionHelper.assertNfsPagetabFiles("S-EPMC125")
            }
        }
}
