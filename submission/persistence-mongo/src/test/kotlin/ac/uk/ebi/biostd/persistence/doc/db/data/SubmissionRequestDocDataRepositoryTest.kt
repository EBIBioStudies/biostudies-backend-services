package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.action
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_VERSION
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.CollectionNames.SUB_RQT_ARCHIVE
import ac.uk.ebi.biostd.persistence.doc.model.CollectionNames.SUB_RQT_FILES_ARCHIVE
import ac.uk.ebi.biostd.persistence.doc.model.DocFilesChanges
import ac.uk.ebi.biostd.persistence.doc.model.DocRequestProcessing
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
            val processingInfo =
                DocRequestProcessing(
                    draftKey = "temp-123",
                    notifyTo = "user@test.org",
                    submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                    totalFiles = 5,
                    fileChanges = DocFilesChanges(10, 3, 12, 8, 5),
                    currentIndex = 6,
                    previousVersion = 1,
                    statusChanges = emptyList(),
                    silentMode = false,
                    singleJobMode = false,
                )
            val request =
                DocSubmissionRequest(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    process = processingInfo,
                    status = RequestStatus.CLEANED,
                    modificationTime = Instant.now().truncatedTo(ChronoUnit.MILLIS),
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
            val files =
                template
                    .find(query, DocSubmissionRequestFile::class.java, SUB_RQT_FILES_ARCHIVE)
                    .asFlow()
                    .toList()
            assertThat(files).containsExactlyInAnyOrder(rqtF1, rqtF2)

            val requests = template.find(query, DocSubmissionRequest::class.java, SUB_RQT_ARCHIVE).asFlow().toList()
            assertThat(requests).containsExactly(request)
        }

    @Test
    fun saveRequestWhenNew() =
        runTest {
            val processingInfo =
                DocRequestProcessing(
                    draftKey = "temp-123",
                    silentMode = false,
                    notifyTo = "user@test.org",
                    submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                    totalFiles = 5,
                    fileChanges = DocFilesChanges(10, 3, 12, 8, 5),
                    currentIndex = 6,
                    previousVersion = 1,
                    statusChanges = emptyList(),
                    singleJobMode = false,
                )
            val rqt =
                DocSubmissionRequest(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    process = processingInfo,
                    status = RequestStatus.CLEANED,
                    modificationTime = Instant.now(),
                )

            val (_, created) = testInstance.saveRequest(rqt)

            assertThat(created).isTrue()
            val newRqt = testInstance.getById(rqt.id)
            assertThat(newRqt.accNo).isEqualTo(rqt.accNo)
            assertThat(newRqt.version).isEqualTo(rqt.version)
            assertThat(newRqt.status).isEqualTo(rqt.status)
            assertThat(newRqt.modificationTime).isCloseTo(rqt.modificationTime, within(100, ChronoUnit.MILLIS))
            assertThat(newRqt.process.draftKey).isEqualTo(rqt.process.draftKey)
            assertThat(newRqt.process.notifyTo).isEqualTo(rqt.process.notifyTo)
            assertThat(newRqt.process.submission).isEqualTo(rqt.process.submission)
            assertThat(newRqt.process.totalFiles).isEqualTo(rqt.process.totalFiles)
            assertThat(newRqt.process.fileChanges.deprecatedFiles).isEqualTo(rqt.process.fileChanges.deprecatedFiles)
            assertThat(newRqt.process.fileChanges.conflictingFiles).isEqualTo(rqt.process.fileChanges.conflictingFiles)
            assertThat(newRqt.process.currentIndex).isEqualTo(rqt.process.currentIndex)
            assertThat(newRqt.process.previousVersion).isEqualTo(rqt.process.previousVersion)
        }

    @Test
    fun saveRequestWhenExists() =
        runTest {
            val processingInfo =
                DocRequestProcessing(
                    draftKey = "temp-123",
                    silentMode = false,
                    notifyTo = "user@test.org",
                    submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                    totalFiles = 5,
                    fileChanges = DocFilesChanges(1, 3, 10, 7, 2),
                    currentIndex = 6,
                    statusChanges = emptyList(),
                    previousVersion = 1,
                    singleJobMode = false,
                )
            val rqt =
                DocSubmissionRequest(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    process = processingInfo,
                    status = RequestStatus.CLEANED,
                    modificationTime = Instant.now(),
                )
            val (existing, _) = testInstance.saveRequest(rqt)
            val newProcessingInfo =
                DocRequestProcessing(
                    draftKey = "temp-987-b",
                    silentMode = false,
                    notifyTo = "user-b@test.org",
                    submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0-b" }.toString()),
                    totalFiles = 51,
                    fileChanges = DocFilesChanges(1, 3, 10, 8, 2),
                    currentIndex = 61,
                    statusChanges = emptyList(),
                    previousVersion = 1,
                    singleJobMode = true,
                )
            val newRequest =
                DocSubmissionRequest(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    status = REQUESTED,
                    process = newProcessingInfo,
                    modificationTime = Instant.now().plusSeconds(10),
                )
            val (_, created) = testInstance.saveRequest(newRequest)

            assertThat(created).isFalse()

            val submissions = testInstance.findByAccNo(newRequest.accNo).toList()
            assertThat(submissions).hasSize(1)

            val request = submissions.first()
            assertThat(request.accNo).isEqualTo(existing.accNo)
            assertThat(request.version).isEqualTo(existing.version)
            assertThat(request.status).isEqualTo(existing.status)
            assertThat(request.process.draftKey).isEqualTo(existing.process.draftKey)
            assertThat(request.process.notifyTo).isEqualTo(existing.process.notifyTo)
            assertThat(request.process.submission).isEqualTo(existing.process.submission)
            assertThat(request.process.totalFiles).isEqualTo(existing.process.totalFiles)
            assertThat(request.process.currentIndex).isEqualTo(existing.process.currentIndex)
            assertThat(request.modificationTime).isCloseTo(existing.modificationTime, within(100, ChronoUnit.MILLIS))
        }

    @Test
    fun loadRequest() =
        runTest {
            val processId = "processId"
            val processingInfo =
                DocRequestProcessing(
                    draftKey = "temp-987-b",
                    silentMode = false,
                    notifyTo = "user-b@test.org",
                    submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0-b" }.toString()),
                    totalFiles = 51,
                    fileChanges = DocFilesChanges(1, 3, 10, 8, 2),
                    currentIndex = 61,
                    statusChanges = emptyList(),
                    singleJobMode = true,
                    previousVersion = 1,
                )
            val rqt =
                DocSubmissionRequest(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    status = REQUESTED,
                    process = processingInfo,
                    modificationTime = Instant.now().plusSeconds(10),
                )
            testInstance.saveRequest(rqt)

            val (changeId, request) = testInstance.getRequest(rqt.accNo, rqt.version, REQUESTED, processId)

            val statusChange = request.process.statusChanges.first { it.statusId.toString() == changeId }
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
