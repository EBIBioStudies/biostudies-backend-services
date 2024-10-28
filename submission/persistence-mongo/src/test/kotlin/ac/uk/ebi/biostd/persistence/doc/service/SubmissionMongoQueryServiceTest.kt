package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.LockConfig
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbServicesConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.migrations.ensureSubmissionIndexes
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocFilesChanges
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ac.uk.ebi.biostd.persistence.doc.test.beans.TestConfig
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_OWNER
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSection
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSection
import com.mongodb.BasicDBObject
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.CLEANED
import ebi.ac.uk.model.RequestStatus.FILES_COPIED
import ebi.ac.uk.model.RequestStatus.LOADED
import ebi.ac.uk.model.RequestStatus.REQUESTED
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSING
import ebi.ac.uk.model.constants.SectionFields.TITLE
import ebi.ac.uk.util.collections.second
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig.extSerializationService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.Duration.ofSeconds
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fullExtSubmission as extSubmission
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSectionAttribute as attribute
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSection as docSection
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission as docSubmission
import ebi.ac.uk.model.RequestStatus.PROCESSED as REQUEST_PROCESSED

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class, MongoDbServicesConfig::class, TestConfig::class, LockConfig::class])
internal class SubmissionMongoQueryServiceTest(
    @Autowired private val toExtSubmissionMapper: ToExtSubmissionMapper,
    @Autowired private val submissionRepo: SubmissionDocDataRepository,
    @Autowired private val requestRepository: SubmissionRequestDocDataRepository,
    @Autowired private val mongoTemplate: ReactiveMongoTemplate,
) {
    private val serializationService: ExtSerializationService = extSerializationService()
    private val testInstance =
        SubmissionMongoPersistenceQueryService(
            submissionRepo,
            toExtSubmissionMapper,
            serializationService,
            requestRepository,
        )

    @AfterEach
    fun afterEach() =
        runBlocking {
            submissionRepo.deleteAll()
            mongoTemplate.ensureSubmissionIndexes()
        }

    @Nested
    inner class FindSubmissions {
        @Test
        fun `find latest by accNo`() =
            runTest {
                submissionRepo.save(docSubmission.copy(accNo = "S-BSST1", version = -1))
                submissionRepo.save(docSubmission.copy(accNo = "S-BSST1", version = 2))

                val result = submissionRepo.findByAccNo("S-BSST1")
                assertThat(result).isNotNull()
                assertThat(result!!.version).isEqualTo(2)
            }

        @Test
        fun `find latest by accNo for submission with old expired version`() =
            runTest {
                submissionRepo.save(docSubmission.copy(accNo = "S-BSST3", version = -1))
                submissionRepo.save(docSubmission.copy(accNo = "S-BSST3", version = -2))
                assertThat(submissionRepo.findByAccNo("S-BSST3")).isNull()
            }

        @Test
        fun `find latest inactive by accNo`() =
            runTest {
                submissionRepo.save(docSubmission.copy(accNo = "S-BSST3", version = -1))
                submissionRepo.save(docSubmission.copy(accNo = "S-BSST3", version = -2))
                val sub = submissionRepo.findFirstByAccNoAndVersionLessThanOrderByVersion(accNo = "S-BSST3")
                assertThat(sub?.version).isEqualTo(-2)
            }
    }

    @Nested
    inner class GetSubmissionsByUser {
        private val section = rootSection.copy(fileList = null, files = listOf(), sections = listOf())

        @BeforeEach
        fun beforeEach(): Unit =
            runBlocking {
                requestRepository.deleteAll()
                submissionRepo.deleteAll()
            }

        @Test
        fun `filtered by accNo`() =
            runTest {
                val subRequest = extSubmission.copy(accNo = "accNo1", version = 2, title = "title1", section = section)
                val savedRequest = saveAsRequest(subRequest, REQUESTED)
                submissionRepo.save(docSubmission.copy(accNo = "accNo1"))

                var result =
                    testInstance.getSubmissionsByUser(
                        SubmissionListFilter(SUBMISSION_OWNER, accNo = "accNo1", limit = 1),
                    )

                assertThat(result).hasSize(1)
                assertThat(result.first()).isEqualTo(savedRequest.asBasicSubmission(PROCESSING))

                result =
                    testInstance.getSubmissionsByUser(
                        SubmissionListFilter(SUBMISSION_OWNER, accNo = "accNo1", limit = 2),
                    )
                assertThat(result).hasSize(1)
                assertThat(result.first()).isEqualTo(savedRequest.asBasicSubmission(PROCESSING))
            }

        @Test
        fun `filtered by keyword on submission title`() =
            runTest {
                val sect1 = section.copy(attributes = listOf(ExtAttribute(TITLE.value, "section title 1")))
                val sect3 = testDocSection.copy(attributes = listOf(DocAttribute(TITLE.value, "section title 3")))

                saveAsRequest(extSubmission.copy(accNo = "acc1", title = "sub title 1", section = sect1), REQUESTED)
                saveAsRequest(extSubmission.copy(accNo = "acc2", title = "wrongT1tl3", section = section), REQUESTED)
                submissionRepo.save(docSubmission.copy(accNo = "acc3", title = "title", section = sect3))

                val result =
                    testInstance.getSubmissionsByUser(
                        SubmissionListFilter(SUBMISSION_OWNER, keywords = "title", limit = 2),
                    )

                assertThat(result).hasSize(2)
                val first = result.first()
                assertThat(first.accNo).isEqualTo("acc1")
                assertThat(first.title).isEqualTo("section title 1")

                val second = result.second()
                assertThat(second.accNo).isEqualTo("acc3")
                assertThat(second.title).isEqualTo("section title 3")
            }

        @Test
        fun `filtered by keyword on section title`() =
            runTest {
                val extSectionMatch = section.copy(attributes = listOf(attribute.copy(name = "Title", value = "match")))
                val extSectionMismatch =
                    section.copy(attributes = listOf(attribute.copy(name = "Title", value = "m_atch")))
                val docSectionMatch =
                    docSection.copy(attributes = listOf(DocAttribute(name = "Title", value = "match")))
                val docSectionNoMatch =
                    docSection.copy(attributes = listOf(DocAttribute(name = "Tit_le", value = "match")))

                saveAsRequest(extSubmission.copy(accNo = "acc1", section = extSectionMatch), REQUESTED)
                saveAsRequest(extSubmission.copy(accNo = "acc2", section = extSectionMismatch), REQUESTED)
                submissionRepo.save(docSubmission.copy(accNo = "acc3", section = docSectionMatch))
                submissionRepo.save(docSubmission.copy(accNo = "acc4", section = docSectionNoMatch))

                val result =
                    testInstance.getSubmissionsByUser(
                        SubmissionListFilter(SUBMISSION_OWNER, keywords = "match", limit = 2),
                    )

                assertThat(result).hasSize(2)
                assertThat(result.first().accNo).isEqualTo("acc1")
                assertThat(result.second().accNo).isEqualTo("acc3")
            }

        @Test
        fun `filtered by type`() =
            runTest {
                val section1 = section.copy(type = "type1")
                val section2 = section.copy(type = "type2")
                val docSection1 = docSection.copy(type = "type1")

                saveAsRequest(extSubmission.copy(accNo = "accNo1", section = section1), REQUESTED)
                saveAsRequest(extSubmission.copy(accNo = "accNo2", section = section2), REQUESTED)
                submissionRepo.save(docSubmission.copy(accNo = "accNo3", section = docSection1))

                val result =
                    testInstance.getSubmissionsByUser(
                        SubmissionListFilter(SUBMISSION_OWNER, type = "type1", limit = 2),
                    )

                assertThat(result).hasSize(2)
                assertThat(result.first().accNo).isEqualTo("accNo1")
                assertThat(result.second().accNo).isEqualTo("accNo3")
            }

        @Test
        fun `filtered by from release time`() =
            runTest {
                val matchDate = OffsetDateTime.of(2010, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
                val mismatchDate = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                saveAsRequest(
                    extSubmission.copy(accNo = "accNo1", releaseTime = matchDate, section = section),
                    REQUESTED,
                )
                saveAsRequest(
                    extSubmission.copy(accNo = "accNo2", releaseTime = mismatchDate, section = section),
                    REQUESTED,
                )
                submissionRepo.save(
                    docSubmission.copy(accNo = "accNo3", releaseTime = matchDate.toInstant()),
                )

                val result =
                    testInstance.getSubmissionsByUser(
                        SubmissionListFilter(
                            SUBMISSION_OWNER,
                            rTimeFrom = OffsetDateTime.of(2005, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                            limit = 2,
                        ),
                    )

                assertThat(result).hasSize(2)
                assertThat(result.first().accNo).isEqualTo("accNo1")
                assertThat(result.second().accNo).isEqualTo("accNo3")
            }

        @Test
        fun `filtered by to release time`() =
            runTest {
                val matchDate = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
                val mismatchDate = OffsetDateTime.of(2010, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

                saveAsRequest(
                    extSubmission.copy(accNo = "accNo1", releaseTime = matchDate, section = section),
                    REQUESTED,
                )
                saveAsRequest(
                    extSubmission.copy(accNo = "accNo2", releaseTime = mismatchDate, section = section),
                    REQUESTED,
                )
                submissionRepo.save(
                    docSubmission.copy(accNo = "accNo3", releaseTime = matchDate.toInstant()),
                )

                val result =
                    testInstance.getSubmissionsByUser(
                        SubmissionListFilter(
                            SUBMISSION_OWNER,
                            rTimeTo = OffsetDateTime.of(2005, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                            limit = 3,
                        ),
                    )

                assertThat(result).hasSize(2)
                assertThat(result.first().accNo).isEqualTo("accNo1")
                assertThat(result.second().accNo).isEqualTo("accNo3")
            }

        @Test
        fun `filtered by released`() =
            runTest {
                saveAsRequest(extSubmission.copy(accNo = "accNo1", released = true, section = section), REQUESTED)
                saveAsRequest(extSubmission.copy(accNo = "accNo2", released = false, section = section), REQUESTED)
                submissionRepo.save(docSubmission.copy(accNo = "accNo3", released = true))

                val result =
                    testInstance.getSubmissionsByUser(
                        SubmissionListFilter(SUBMISSION_OWNER, released = true, limit = 2),
                    )

                assertThat(result).hasSize(2)
                assertThat(result.first().accNo).isEqualTo("accNo1")
                assertThat(result.second().accNo).isEqualTo("accNo3")
            }

        @Test
        fun `when all`() =
            runTest {
                submissionRepo.save(docSubmission.copy(accNo = "accNo1", version = 1))
                submissionRepo.save(docSubmission.copy(accNo = "accNo2", version = 1))
                submissionRepo.save(docSubmission.copy(accNo = "accNo3", version = 1))
                submissionRepo.save(docSubmission.copy(accNo = "accNo4", version = 1))
                submissionRepo.save(docSubmission.copy(accNo = "accNo5", version = 1))

                saveAsRequest(extSubmission.copy(accNo = "accNo1", version = 2), REQUESTED)
                saveAsRequest(extSubmission.copy(accNo = "accNo2", version = 2), LOADED)
                saveAsRequest(extSubmission.copy(accNo = "accNo3", version = 2), CLEANED)
                saveAsRequest(extSubmission.copy(accNo = "accNo4", version = 2), FILES_COPIED)

                val result = testInstance.getSubmissionsByUser(SubmissionListFilter(SUBMISSION_OWNER))

                assertThat(result).hasSize(5)
                assertThat(result[0].accNo).isEqualTo("accNo1")
                assertThat(result[0].version).isEqualTo(2)
                assertThat(result[0].status).isEqualTo(PROCESSING)
                assertThat(
                    requestRepository.existsByAccNoAndStatusIn(
                        "accNo1",
                        RequestStatus.PROCESSING_STATUS,
                    ),
                ).isTrue()

                assertThat(result[1].accNo).isEqualTo("accNo2")
                assertThat(result[1].version).isEqualTo(2)
                assertThat(result[1].status).isEqualTo(PROCESSING)
                assertThat(
                    requestRepository.existsByAccNoAndStatusIn(
                        "accNo2",
                        RequestStatus.PROCESSING_STATUS,
                    ),
                ).isTrue()

                assertThat(result[2].accNo).isEqualTo("accNo3")
                assertThat(result[2].version).isEqualTo(2)
                assertThat(result[2].status).isEqualTo(PROCESSING)
                assertThat(
                    requestRepository.existsByAccNoAndStatusIn(
                        "accNo3",
                        RequestStatus.PROCESSING_STATUS,
                    ),
                ).isTrue()

                assertThat(result[3].accNo).isEqualTo("accNo4")
                assertThat(result[3].version).isEqualTo(2)
                assertThat(result[3].status).isEqualTo(PROCESSING)
                assertThat(
                    requestRepository.existsByAccNoAndStatusIn(
                        "accNo4",
                        RequestStatus.PROCESSING_STATUS,
                    ),
                ).isTrue()

                assertThat(result[4].accNo).isEqualTo("accNo5")
                assertThat(result[4].version).isEqualTo(1)
                assertThat(result[4].status).isEqualTo(PROCESSED)
                assertThat(
                    requestRepository.existsByAccNoAndStatusIn(
                        "accNo5",
                        RequestStatus.PROCESSING_STATUS,
                    ),
                ).isFalse()
            }

        @Test
        fun `get greatest version submission`() =
            runTest {
                val sub1 = submissionRepo.save(docSubmission.copy(accNo = "accNo1", version = 3))
                submissionRepo.save(docSubmission.copy(accNo = "accNo1", version = -2))
                submissionRepo.save(docSubmission.copy(accNo = "accNo1", version = -1))

                val result =
                    testInstance.getSubmissionsByUser(
                        SubmissionListFilter(SUBMISSION_OWNER, accNo = "accNo1", limit = 3),
                    )

                assertThat(result).hasSize(1)
                assertThat(result.first()).isEqualTo(sub1.asBasicSubmission(PROCESSED))
            }

        @Test
        fun `get only requests with status REQUESTED`() =
            runTest {
                saveAsRequest(extSubmission.copy(accNo = "accNo1", title = "one", section = section), REQUESTED)
                saveAsRequest(extSubmission.copy(accNo = "accNo1", title = "two", section = section), REQUEST_PROCESSED)
                saveAsRequest(
                    extSubmission.copy(accNo = "accNo1", title = "three", section = section),
                    REQUEST_PROCESSED,
                )

                val result =
                    testInstance.getSubmissionsByUser(
                        SubmissionListFilter(SUBMISSION_OWNER, accNo = "accNo1", limit = 3),
                    )

                assertThat(result).hasSize(1)
                assertThat(result.first().title).isEqualTo("one")
            }

        private suspend fun saveAsRequest(
            extSubmission: ExtSubmission,
            status: RequestStatus,
        ): ExtSubmission {
            requestRepository.saveRequest(asRequest(extSubmission, status))
            return extSubmission
        }

        private fun asRequest(
            submission: ExtSubmission,
            status: RequestStatus,
        ) = DocSubmissionRequest(
            id = ObjectId(),
            accNo = submission.accNo,
            version = submission.version,
            status = status,
            draftKey = null,
            silentMode = false,
            processAll = false,
            notifyTo = submission.owner,
            submission = BasicDBObject.parse(serializationService.serialize(submission)),
            totalFiles = 6,
            fileChanges = DocFilesChanges(1, 0, 10, 2, 2),
            currentIndex = 0,
            modificationTime = Instant.now(),
            statusChanges = emptyList(),
            previousVersion = 1,
        )
    }

    @Test
    fun `get non existing submission`() =
        runTest {
            val exception = assertThrows<SubmissionNotFoundException> { testInstance.getExtByAccNo("S-BSST3") }
            assertThat(exception.message).isEqualTo("The submission 'S-BSST3' was not found")
        }

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
