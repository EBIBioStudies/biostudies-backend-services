package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestFilesDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.LockConfig
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocFilesChanges
import ac.uk.ebi.biostd.persistence.doc.model.DocRequestProcessing
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fullExtSubmission
import com.mongodb.BasicDBObject
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.CLEANED
import ebi.ac.uk.model.RequestStatus.FILES_COPIED
import ebi.ac.uk.model.RequestStatus.PERSISTED
import ebi.ac.uk.model.RequestStatus.PROCESSED
import ebi.ac.uk.model.RequestStatus.REQUESTED
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.junit5.MockKExtension
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.Duration.ofSeconds
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExtendWith(MockKExtension::class, SpringExtension::class, TemporaryFolderExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class, LockConfig::class])
class SubmissionRequestMongoPersistenceServiceTest(
    private val tempFolder: TemporaryFolder,
    @Autowired private val requestRepository: SubmissionRequestDocDataRepository,
    @Autowired private val requestFilesRepository: SubmissionRequestFilesDocDataRepository,
    @Autowired private val lockService: DistributedLockService,
) {
    private val testInstance =
        SubmissionRequestMongoPersistenceService(
            ExtSerializationService(),
            requestRepository,
            requestFilesRepository,
            lockService,
        )

    @AfterEach
    fun afterEach() =
        runBlocking {
            requestRepository.deleteAll()
            unmockkStatic(Instant::class)
        }

    @Nested
    inner class ProcessRequest {
        @Test
        fun onSuccess() =
            runTest {
                val submission = fullExtSubmission
                val rqt =
                    SubmissionRequest(
                        accNo = "S-BSST1",
                        version = 1,
                        owner = "owner@mail.com",
                        submission = submission,
                        notifyTo = "notifyTo",
                        silentMode = false,
                        singleJobMode = false,
                        files = emptyList(),
                        preferredSources = listOf(SUBMISSION),
                        onBehalfUser = null,
                        previousVersion = null,
                    )

                val (accNo, version) = testInstance.saveRequest(rqt)
                assertThat(accNo).isEqualTo("S-BSST1")
                assertThat(version).isEqualTo(1)

                var operation = 0

                testInstance.onRequest(
                    accNo,
                    version,
                    REQUESTED,
                    "processId",
                ) {
                    operation++
                    it.copy(status = PERSISTED)
                }

                assertThat(operation).isOne()
                val request = requestRepository.getByAccNoAndVersion(accNo, version)
                assertThat(request.status).isEqualTo(PERSISTED)
                assertThat(request.process!!.statusChanges).hasSize(1)

                val statusChange = request.process!!.statusChanges.first()
                assertThat(statusChange.processId).isEqualTo("processId")
                assertThat(statusChange.startTime).isNotNull()
                assertThat(statusChange.endTime).isNotNull()
                assertThat(statusChange.result).isEqualTo("SUCCESS")
            }

        @Test
        fun onFailure() =
            runTest {
                val submission = fullExtSubmission
                val rqt =
                    SubmissionRequest(
                        accNo = "S-BSST1",
                        version = 1,
                        owner = "owner@mail.com",
                        submission = submission,
                        notifyTo = "notifyTo",
                        silentMode = false,
                        singleJobMode = false,
                        files = emptyList(),
                        preferredSources = listOf(SUBMISSION),
                        onBehalfUser = null,
                        previousVersion = null,
                    )

                val (accNo, version) = testInstance.saveRequest(rqt)
                assertThat(accNo).isEqualTo("S-BSST1")
                assertThat(version).isEqualTo(1)

                val exception = IllegalStateException("opps something wrong")
                val throwException =
                    assertThrows<IllegalStateException> {
                        testInstance.onRequest(accNo, version, REQUESTED, "processId", { throw exception })
                    }

                assertThat(throwException).isEqualTo(exception)
                val request = requestRepository.getByAccNoAndVersion(accNo, version)
                assertThat(request.status).isEqualTo(REQUESTED)
                assertThat(request.process!!.statusChanges).hasSize(1)

                val statusChange = request.process!!.statusChanges.first()
                assertThat(statusChange.processId).isEqualTo("processId")
                assertThat(statusChange.startTime).isNotNull()
                assertThat(statusChange.endTime).isNotNull()
                assertThat(statusChange.result).isEqualTo("ERROR")
            }
    }

    @Test
    fun archiveRequest() =
        runTest {
            val request =
                DocSubmissionRequest(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    owner = "owner@mail.org",
                    draft = "draft-content",
                    status = PROCESSED,
                    modificationTime = Instant.now().truncatedTo(ChronoUnit.MILLIS),
                    onBehalfUser = null,
                    files = emptyList(),
                    preferredSources = listOf(SUBMISSION.name),
                    errors = emptyList(),
                    process =
                        DocRequestProcessing(
                            notifyTo = "user@test.org",
                            submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                            totalFiles = 5,
                            fileChanges = DocFilesChanges(10, 3, 12, 8, 5),
                            currentIndex = 6,
                            previousVersion = 1,
                            statusChanges = emptyList(),
                            silentMode = false,
                            singleJobMode = false,
                        ),
                )
            val rqtF1 =
                DocSubmissionRequestFile(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    path = "file-path",
                    index = 1,
                    status = LOADED,
                    previousSubFile = false,
                    file = BasicDBObject("property", "value"),
                )

            requestRepository.saveRequest(request)
            requestFilesRepository.save(rqtF1)

            testInstance.archiveRequest("abc-123", 2)

            assertThat(requestRepository.findByAccNo("abc-123").toList()).isEmpty()
            assertThat(requestFilesRepository.countByAccNoAndVersion("abc-123", 2)).isZero()
        }

    @Test
    fun getProcessingRequests() =
        runTest {
            fun testRequest(
                accNo: String,
                version: Int,
                modificationTime: Instant,
                status: RequestStatus,
            ) = DocSubmissionRequest(
                id = ObjectId(),
                accNo = accNo,
                version = version,
                owner = "owner@mail.org",
                draft = null,
                status = status,
                modificationTime = modificationTime,
                onBehalfUser = null,
                files = emptyList(),
                preferredSources = listOf(SUBMISSION.name),
                errors = emptyList(),
                process =
                    DocRequestProcessing(
                        silentMode = false,
                        singleJobMode = false,
                        notifyTo = "user@test.org",
                        submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                        totalFiles = 5,
                        fileChanges = DocFilesChanges(1, 0, 10, 3, 2),
                        currentIndex = 0,
                        statusChanges = emptyList(),
                        previousVersion = 1,
                    ),
            )

            requestRepository.save(testRequest("abc", 1, Instant.now().minusSeconds(10), CLEANED))
            requestRepository.save(testRequest("zxy", 2, Instant.now().minusSeconds(20), FILES_COPIED))

            assertThat(testInstance.getActiveRequests().toList()).containsExactly("abc" to 1, "zxy" to 2)
            assertThat(testInstance.getActiveRequests(ofSeconds(5)).toList()).containsExactly(
                "abc" to 1,
                "zxy" to 2,
            )
            assertThat(testInstance.getActiveRequests(ofSeconds(15)).toList()).containsExactly("zxy" to 2)
        }

    @Test
    fun `update requestFiles`() =
        runTest {
            val extFile1 = createNfsFile("rqt1.txt", "Files/rqt1.txt", tempFolder.createFile("rqt1.txt"))
            val extFile2 = createNfsFile("rqt2.txt", "Files/rqt2.txt", tempFolder.createFile("rqt2.txt"))
            val rqtFile1 = SubmissionRequestFile("S-BSST0", 1, index = 1, "rqt1.txt", extFile1, INDEXED)
            val rqtFile2 = SubmissionRequestFile("S-BSST0", 1, index = 2, "rqt2.txt", extFile2, INDEXED)

            requestRepository.upsertSubRqtFile(rqtFile1)
            requestRepository.upsertSubRqtFile(rqtFile2)
            requestRepository.save(testRequest())

            val updatedRqtFile1 = rqtFile1.copy(file = extFile1.copy(md5 = "changedMd5-1"), status = LOADED)
            val updatedRqtFile2 = rqtFile2.copy(file = extFile2.copy(md5 = "changedMd5-2"), status = LOADED)
            testInstance.updateRqtFiles(listOf(updatedRqtFile1, updatedRqtFile2))

            val request = requestRepository.getByAccNoAndVersion("S-BSST0", 1)
            assertThat(request.modificationTime).isNotNull()
            assertThat(request.process!!.currentIndex).isEqualTo(2)

            val savedFile1 = requestFilesRepository.getByPathAndAccNoAndVersion(rqtFile1.path, "S-BSST0", 1)
            assertThat(savedFile1.file.get("md5")).isEqualTo("changedMd5-1")
            assertThat(savedFile1.status).isEqualTo(LOADED)

            val savedFile2 = requestFilesRepository.getByPathAndAccNoAndVersion(rqtFile2.path, "S-BSST0", 1)
            assertThat(savedFile2.file.get("md5")).isEqualTo("changedMd5-2")
            assertThat(savedFile2.status).isEqualTo(LOADED)
        }

    private fun testRequest() =
        DocSubmissionRequest(
            id = ObjectId(),
            accNo = "S-BSST0",
            version = 1,
            owner = "owner@mail.org",
            draft = null,
            status = CLEANED,
            modificationTime = Instant.ofEpochMilli(1664981300),
            onBehalfUser = null,
            files = emptyList(),
            preferredSources = listOf(SUBMISSION.name),
            errors = emptyList(),
            process =
                DocRequestProcessing(
                    silentMode = false,
                    singleJobMode = false,
                    notifyTo = "user@test.org",
                    submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                    totalFiles = 5,
                    fileChanges = DocFilesChanges(1, 0, 10, 2, 2),
                    currentIndex = 0,
                    statusChanges = emptyList(),
                    previousVersion = 1,
                ),
        )

    companion object {
        @Container
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
