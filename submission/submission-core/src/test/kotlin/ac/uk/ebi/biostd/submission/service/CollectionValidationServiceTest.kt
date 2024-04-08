package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidationService
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidator
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.BeanFactory
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
class CollectionValidationServiceTest(
    @MockK private val beanFactory: BeanFactory,
    @MockK private val queryService: SubmissionMetaQueryService,
) {
    private val testInstance = CollectionValidationService(beanFactory, queryService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `execute collection validators`(
        @MockK validator: CollectionValidator,
    ) = runTest {
        val submission = basicExtSubmission.copy(collections = listOf(ExtCollection("ArrayExpress")))
        val basicProject =
            BasicCollection(
                accNo = "ArrayExpress",
                accNoPattern = "!{E-MTAB}",
                collections = listOf("ArrayExpress"),
                validator = "ArrayExpressValidator",
                releaseTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, UTC),
            )

        every { validator.validate(submission) } answers { nothing }
        coEvery { queryService.getBasicCollection("ArrayExpress") } returns basicProject
        every { beanFactory.getBean("ArrayExpressValidator", CollectionValidator::class.java) } returns validator

        testInstance.executeCollectionValidators(submission)

        verify(exactly = 1) { validator.validate(submission) }
    }

    @Test
    fun `execute collection validators over new collection`(
        @MockK validator: CollectionValidator,
    ) = runTest {
        val submission =
            basicExtSubmission.copy(
                section = ExtSection(type = "Project"),
                collections = listOf(ExtCollection("ArrayExpress")),
            )

        testInstance.executeCollectionValidators(submission)

        coVerify(exactly = 0) {
            validator.validate(submission)
            queryService.getBasicCollection("ArrayExpress")
        }
    }
}
