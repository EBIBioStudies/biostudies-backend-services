package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.integration.LockConfig
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fullExtSubmission
import com.mongodb.BasicDBObject
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.createNfsFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.junit5.MockKExtension
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@ExtendWith(MockKExtension::class, SpringExtension::class, TemporaryFolderExtension::class)
@Testcontainers
@OptIn(ExperimentalCoroutinesApi::class)
@SpringBootTest(classes = [MongoDbReposConfig::class, LockConfig::class])
class SubmissionRequestMongoPersistenceServiceTest(
    private val tempFolder: TemporaryFolder,
    @Autowired private val requestRepository: SubmissionRequestDocDataRepository,
    @Autowired private val requestFilesRepository: SubmissionRequestFilesRepository,
    @Autowired private val lockService: DistributedLockService,
) {
    private val testInstance =
        SubmissionRequestMongoPersistenceService(ExtSerializationService(), requestRepository, lockService)

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
                        submission = submission,
                        notifyTo = "notifyTo",
                        draftKey = "draftKey",
                    )

                val (accNo, version) = testInstance.createRequest(rqt)
                assertThat(submission.accNo).isEqualTo(accNo)
                assertThat(submission.version).isEqualTo(version)

                var operation = 0

                testInstance.onRequest(
                    accNo,
                    version,
                    REQUESTED,
                    "processId",
                    {
                        operation++
                        RqtUpdate(it.copy(status = PERSISTED))
                    },
                )

                assertThat(operation).isOne()
                val request = requestRepository.getByAccNoAndVersion(accNo, version)
                assertThat(request.status).isEqualTo(PERSISTED)
                assertThat(request.statusChanges).hasSize(1)

                val statusChange = request.statusChanges.first()
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
                        submission = submission,
                        notifyTo = "notifyTo",
                        draftKey = "draftKey",
                    )

                val (accNo, version) = testInstance.createRequest(rqt)
                assertThat(submission.accNo).isEqualTo(accNo)
                assertThat(submission.version).isEqualTo(version)

                val exception = IllegalStateException("opps something wrong")
                val throwException =
                    assertThrows<IllegalStateException> {
                        testInstance.onRequest<RqtUpdate>(accNo, version, REQUESTED, "processId", { throw exception })
                    }

                assertThat(throwException).isEqualTo(exception)
                val request = requestRepository.getByAccNoAndVersion(accNo, version)
                assertThat(request.status).isEqualTo(REQUESTED)
                assertThat(request.statusChanges).hasSize(1)

                val statusChange = request.statusChanges.first()
                assertThat(statusChange.processId).isEqualTo("processId")
                assertThat(statusChange.startTime).isNotNull()
                assertThat(statusChange.endTime).isNotNull()
                assertThat(statusChange.result).isEqualTo("ERROR")
            }
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
                status = status,
                draftKey = null,
                notifyTo = "user@test.org",
                submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                totalFiles = 5,
                currentIndex = 0,
                modificationTime = modificationTime,
                statusChanges = emptyList(),
            )

            requestRepository.save(testRequest("abc", 1, Instant.now().minusSeconds(10), CLEANED))
            requestRepository.save(testRequest("zxy", 2, Instant.now().minusSeconds(20), FILES_COPIED))

            assertThat(testInstance.getProcessingRequests().toList()).containsExactly("abc" to 1, "zxy" to 2)
            assertThat(testInstance.getProcessingRequests(ofSeconds(5)).toList()).containsExactly(
                "abc" to 1,
                "zxy" to 2,
            )
            assertThat(testInstance.getProcessingRequests(ofSeconds(15)).toList()).containsExactly("zxy" to 2)
        }

    @Test
    fun `update requestFile`() =
        runTest {
            val extFile = createNfsFile("requested.txt", "Files/requested.txt", tempFolder.createFile("requested.txt"))
            val requestFile = SubmissionRequestFile("S-BSST0", 1, index = 2, "requested.txt", extFile, INDEXED)

            requestRepository.upsertSubmissionRequestFile(requestFile)
            requestRepository.save(testRequest())

            testInstance.updateRqtFile(requestFile.copy(file = extFile.copy(md5 = "changedMd5"), status = LOADED))

            val request = requestRepository.getByAccNoAndVersion("S-BSST0", 1)
            assertThat(request.modificationTime).isNotNull()
            assertThat(request.currentIndex).isEqualTo(1)

            val savedFile = requestFilesRepository.getByPathAndAccNoAndVersion(requestFile.path, "S-BSST0", 1)
            assertThat(savedFile.file.get("md5")).isEqualTo("changedMd5")
            assertThat(savedFile.status).isEqualTo(LOADED)
        }

    private fun testRequest() =
        DocSubmissionRequest(
            id = ObjectId(),
            accNo = "S-BSST0",
            version = 1,
            status = CLEANED,
            draftKey = null,
            notifyTo = "user@test.org",
            submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
            totalFiles = 5,
            currentIndex = 0,
            modificationTime = Instant.ofEpochMilli(1664981300),
            statusChanges = emptyList(),
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
