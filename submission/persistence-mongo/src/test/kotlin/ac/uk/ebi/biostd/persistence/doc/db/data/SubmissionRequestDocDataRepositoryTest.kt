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
import ebi.ac.uk.model.RequestStatus.CLEANED
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
                    owner = "owner@mail.org",
                    draft = "draft-content",
                    status = CLEANED,
                    modificationTime = Instant.now().truncatedTo(ChronoUnit.MILLIS),
                    onBehalfUser = null,
                    files = emptyList(),
                    preferredSources = listOf("SUBMISSION"),
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
            val rqt =
                DocSubmissionRequest(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    owner = "owner@mail.org",
                    draft = "draft-content",
                    status = CLEANED,
                    modificationTime = Instant.now(),
                    onBehalfUser = null,
                    files = emptyList(),
                    preferredSources = listOf("SUBMISSION"),
                    errors = emptyList(),
                    process =
                        DocRequestProcessing(
                            silentMode = false,
                            notifyTo = "user@test.org",
                            submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                            totalFiles = 5,
                            fileChanges = DocFilesChanges(10, 3, 12, 8, 5),
                            currentIndex = 6,
                            previousVersion = 1,
                            statusChanges = emptyList(),
                            singleJobMode = false,
                        ),
                )

            testInstance.saveRequest(rqt)

            val newRqt = testInstance.getById(rqt.id)
            assertThat(newRqt.accNo).isEqualTo(rqt.accNo)
            assertThat(newRqt.version).isEqualTo(rqt.version)
            assertThat(newRqt.status).isEqualTo(rqt.status)
            assertThat(newRqt.draft).isEqualTo(rqt.draft)

            val process = rqt.process!!
            val newProcess = newRqt.process!!
            assertThat(newProcess.notifyTo).isEqualTo(process.notifyTo)
            assertThat(newProcess.submission).isEqualTo(process.submission)
            assertThat(newProcess.totalFiles).isEqualTo(process.totalFiles)
            assertThat(newProcess.fileChanges.deprecatedFiles).isEqualTo(process.fileChanges.deprecatedFiles)
            assertThat(newProcess.fileChanges.conflictingFiles).isEqualTo(process.fileChanges.conflictingFiles)
            assertThat(newProcess.currentIndex).isEqualTo(process.currentIndex)
            assertThat(newProcess.previousVersion).isEqualTo(process.previousVersion)
            assertThat(newRqt.modificationTime).isCloseTo(rqt.modificationTime, within(100, ChronoUnit.MILLIS))
        }

    @Test
    fun saveRequestWhenExists() =
        runTest {
            val existing =
                testInstance.saveRequest(
                    DocSubmissionRequest(
                        id = ObjectId(),
                        accNo = "abc-123",
                        version = 2,
                        owner = "owner@mail.org",
                        draft = "draft-content",
                        status = CLEANED,
                        modificationTime = Instant.now(),
                        onBehalfUser = null,
                        files = emptyList(),
                        preferredSources = listOf("SUBMISSION"),
                        errors = emptyList(),
                        process =
                            DocRequestProcessing(
                                silentMode = false,
                                notifyTo = "user@test.org",
                                submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0" }.toString()),
                                totalFiles = 5,
                                fileChanges = DocFilesChanges(1, 3, 10, 7, 2),
                                currentIndex = 6,
                                statusChanges = emptyList(),
                                previousVersion = 1,
                                singleJobMode = false,
                            ),
                    ),
                )

            val newRequest =
                DocSubmissionRequest(
                    id = ObjectId(),
                    accNo = "abc-123",
                    version = 2,
                    owner = "owner@mail.org",
                    draft = "draft-content",
                    status = REQUESTED,
                    modificationTime = Instant.now().plusSeconds(10),
                    onBehalfUser = null,
                    files = emptyList(),
                    preferredSources = listOf("SUBMISSION"),
                    errors = emptyList(),
                    process =
                        DocRequestProcessing(
                            silentMode = false,
                            notifyTo = "user-b@test.org",
                            submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0-b" }.toString()),
                            totalFiles = 51,
                            fileChanges = DocFilesChanges(1, 3, 10, 8, 2),
                            currentIndex = 61,
                            statusChanges = emptyList(),
                            previousVersion = 1,
                            singleJobMode = true,
                        ),
                )
            testInstance.saveRequest(newRequest)

            val submissions = testInstance.findByAccNo(newRequest.accNo).toList()
            assertThat(submissions).hasSize(1)

            val request = submissions.first()
            assertThat(request.owner).isEqualTo(existing.owner)
            assertThat(request.draft).isEqualTo(existing.draft)
            assertThat(request.accNo).isEqualTo(newRequest.accNo)
            assertThat(request.version).isEqualTo(newRequest.version)
            assertThat(request.status).isEqualTo(newRequest.status)
            assertThat(request.process!!.notifyTo).isEqualTo(newRequest.process!!.notifyTo)
            assertThat(request.process!!.submission).isEqualTo(newRequest.process!!.submission)
            assertThat(request.process!!.totalFiles).isEqualTo(newRequest.process!!.totalFiles)
            assertThat(request.process!!.currentIndex).isEqualTo(newRequest.process!!.currentIndex)
            assertThat(request.modificationTime).isCloseTo(newRequest.modificationTime, within(100, ChronoUnit.MILLIS))
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
                    owner = "owner@mail.org",
                    draft = "draft-content",
                    status = REQUESTED,
                    modificationTime = Instant.now().plusSeconds(10),
                    onBehalfUser = null,
                    files = emptyList(),
                    preferredSources = listOf("SUBMISSION"),
                    errors = emptyList(),
                    process =
                        DocRequestProcessing(
                            silentMode = false,
                            notifyTo = "user-b@test.org",
                            submission = BasicDBObject.parse(jsonObj { "submission" to "S-BSST0-b" }.toString()),
                            totalFiles = 51,
                            fileChanges = DocFilesChanges(1, 3, 10, 8, 2),
                            currentIndex = 61,
                            statusChanges = emptyList(),
                            singleJobMode = true,
                            previousVersion = 1,
                        ),
                )
            testInstance.saveRequest(rqt)

            val (changeId, request) = testInstance.getRequest(rqt.accNo, rqt.version, REQUESTED, processId)

            val statusChange = request.process!!.statusChanges.first { it.statusId.toString() == changeId }
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
