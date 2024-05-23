package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.model.action
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.jsonObj
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
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
import java.time.Duration.ofSeconds
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionRequestDocDataRepositoryTest(
    @Autowired val testInstance: SubmissionRequestDocDataRepository,
) {
    @AfterEach
    fun afterEach() =
        runBlocking {
            testInstance.deleteAll()
        }

    @Test
    fun saveRequestWhenNew() =
        runTest {
            val request =
                DocSubmissionRequest(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    status = RequestStatus.CLEANED,
                    draftKey = "temp-123",
                    notifyTo = "user@test.org",
                    submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                    totalFiles = 5,
                    deprecatedFiles = 10,
                    conflictingFiles = 12,
                    currentIndex = 6,
                    modificationTime = Instant.now(),
                    previousVersion = 1,
                    statusChanges = emptyList(),
                )

            val (_, created) = testInstance.saveRequest(request)

            assertThat(created).isTrue()
            val newRequest = testInstance.getById(request.id)
            assertThat(newRequest.accNo).isEqualTo(request.accNo)
            assertThat(newRequest.version).isEqualTo(request.version)
            assertThat(newRequest.status).isEqualTo(request.status)
            assertThat(newRequest.draftKey).isEqualTo(request.draftKey)
            assertThat(newRequest.notifyTo).isEqualTo(request.notifyTo)
            assertThat(newRequest.submission).isEqualTo(request.submission)
            assertThat(newRequest.totalFiles).isEqualTo(request.totalFiles)
            assertThat(newRequest.deprecatedFiles).isEqualTo(request.deprecatedFiles)
            assertThat(newRequest.conflictingFiles).isEqualTo(request.conflictingFiles)
            assertThat(newRequest.currentIndex).isEqualTo(request.currentIndex)
            assertThat(newRequest.modificationTime).isCloseTo(request.modificationTime, within(100, ChronoUnit.MILLIS))
            assertThat(newRequest.previousVersion).isEqualTo(request.previousVersion)
        }

    @Test
    fun saveRequestWhenExists() =
        runTest {
            val (existing, _) =
                testInstance.saveRequest(
                    DocSubmissionRequest(
                        id = ObjectId(),
                        accNo = "abc-123",
                        version = 2,
                        status = RequestStatus.CLEANED,
                        draftKey = "temp-123",
                        notifyTo = "user@test.org",
                        submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                        totalFiles = 5,
                        deprecatedFiles = 10,
                        conflictingFiles = 1,
                        currentIndex = 6,
                        modificationTime = Instant.now(),
                        statusChanges = emptyList(),
                        previousVersion = 1,
                    ),
                )

            val newRequest =
                DocSubmissionRequest(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    status = REQUESTED,
                    draftKey = "temp-987-b",
                    notifyTo = "user-b@test.org",
                    submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0-b" }.toString()),
                    totalFiles = 51,
                    deprecatedFiles = 10,
                    conflictingFiles = 1,
                    currentIndex = 61,
                    modificationTime = Instant.now().plusSeconds(10),
                    statusChanges = emptyList(),
                    previousVersion = 1,
                )
            val (_, created) = testInstance.saveRequest(newRequest)

            assertThat(created).isFalse()

            val submissions = testInstance.findByAccNo(newRequest.accNo).toList()
            assertThat(submissions).hasSize(1)

            val request = submissions.first()
            assertThat(request.accNo).isEqualTo(existing.accNo)
            assertThat(request.version).isEqualTo(existing.version)
            assertThat(request.status).isEqualTo(existing.status)
            assertThat(request.draftKey).isEqualTo(existing.draftKey)
            assertThat(request.notifyTo).isEqualTo(existing.notifyTo)
            assertThat(request.submission).isEqualTo(existing.submission)
            assertThat(request.totalFiles).isEqualTo(existing.totalFiles)
            assertThat(request.currentIndex).isEqualTo(existing.currentIndex)
            assertThat(request.modificationTime).isCloseTo(existing.modificationTime, within(100, ChronoUnit.MILLIS))
        }

    @Test
    fun loadRequest() =
        runTest {
            val procesId = "processId"
            val rqt =
                DocSubmissionRequest(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    status = REQUESTED,
                    draftKey = "temp-987-b",
                    notifyTo = "user-b@test.org",
                    submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0-b" }.toString()),
                    totalFiles = 51,
                    deprecatedFiles = 10,
                    conflictingFiles = 1,
                    currentIndex = 61,
                    modificationTime = Instant.now().plusSeconds(10),
                    statusChanges = emptyList(),
                    previousVersion = 1,
                )
            testInstance.saveRequest(rqt)

            val (changeId, request) = testInstance.getRequest(rqt.accNo, rqt.version, REQUESTED, procesId)

            val statusChange = request.statusChanges.filter { it.statusId.toString() == changeId }.first()
            assertThat(statusChange.status).isEqualTo(REQUESTED.action)
            assertThat(statusChange.startTime).isNotNull()
            assertThat(statusChange.endTime).isNull()
        }

    private companion object {
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
