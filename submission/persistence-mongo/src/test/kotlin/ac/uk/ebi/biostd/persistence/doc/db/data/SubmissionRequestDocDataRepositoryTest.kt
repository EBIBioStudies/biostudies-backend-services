package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.action
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_VERSION
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.CollectionsNames.RQT_ARCH_COL
import ac.uk.ebi.biostd.persistence.doc.model.CollectionsNames.RQT_FILE_ARCH_COL
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import com.mongodb.BasicDBObject
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.REQUESTED
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
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
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
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
    @Autowired val filesRepository: SubmissionRequestFilesDocDataRepository,
    @Autowired val template: ReactiveMongoTemplate,
) {
    @AfterEach
    fun afterEach() =
        runBlocking {
            testInstance.deleteAll()
        }

    @Test
    fun archiveRequest() =
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
                    deprecatedPageTab = 3,
                    conflictingFiles = 12,
                    conflictingPageTab = 8,
                    reusedFiles = 5,
                    currentIndex = 6,
                    modificationTime = Instant.now().truncatedTo(ChronoUnit.MILLIS),
                    previousVersion = 1,
                    statusChanges = emptyList(),
                )
            val rqtF1 =
                DocSubmissionRequestFile(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    path = "file-path",
                    index = 1,
                    status = RequestFileStatus.LOADED,
                    previousSubFile = false,
                    file = BasicDBObject("property", "value"),
                )
            val rqtF2 =
                DocSubmissionRequestFile(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    path = "file-path-2",
                    index = 2,
                    status = RequestFileStatus.CLEANED,
                    previousSubFile = false,
                    file = BasicDBObject("property", "value-2"),
                )

            filesRepository.save(rqtF1)
            filesRepository.save(rqtF2)
            testInstance.saveRequest(request)

            val result = testInstance.archiveRequest("abc-123", 2)
            assertThat(result).isEqualTo(2)

            val query = Query().addCriteria(where(RQT_ACC_NO).`is`("abc-123").andOperator(where(RQT_VERSION).`is`(2)))
            val files = template.find(query, DocSubmissionRequestFile::class.java, RQT_FILE_ARCH_COL).asFlow().toList()
            assertThat(files).containsExactlyInAnyOrder(rqtF1, rqtF2)

            val requests = template.find(query, DocSubmissionRequest::class.java, RQT_ARCH_COL).asFlow().toList()
            assertThat(requests).containsExactly(request)
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
                    deprecatedPageTab = 3,
                    conflictingFiles = 12,
                    conflictingPageTab = 8,
                    reusedFiles = 5,
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
                        conflictingFiles = 1,
                        conflictingPageTab = 3,
                        deprecatedFiles = 10,
                        deprecatedPageTab = 7,
                        reusedFiles = 2,
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
                    conflictingFiles = 1,
                    conflictingPageTab = 3,
                    deprecatedFiles = 10,
                    deprecatedPageTab = 8,
                    reusedFiles = 2,
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
            val processId = "processId"
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
                    conflictingFiles = 1,
                    conflictingPageTab = 3,
                    deprecatedFiles = 10,
                    deprecatedPageTab = 8,
                    reusedFiles = 2,
                    currentIndex = 61,
                    modificationTime = Instant.now().plusSeconds(10),
                    statusChanges = emptyList(),
                    previousVersion = 1,
                )
            testInstance.saveRequest(rqt)

            val (changeId, request) = testInstance.getRequest(rqt.accNo, rqt.version, REQUESTED, processId)

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
