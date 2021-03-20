package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.submission.exceptions.CollectionInvalidAccessTagException
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidator
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.BeanFactory
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
class ParentInfoServiceTest(
    @MockK private val beanFactory: BeanFactory,
    @MockK private val queryService: SubmissionMetaQueryService
) {
    private val testInstance = ParentInfoService(beanFactory, queryService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `no parent`() {
        val parentInfo = testInstance.getParentInfo(null)
        assertThat(parentInfo.accessTags).isEmpty()
        assertThat(parentInfo.releaseTime).isNull()
        assertThat(parentInfo.parentTemplate).isNull()
    }

    @Test
    fun `get parent info`() {
        val parentAccNo = "BioImages-EMPIAR"
        val testTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, UTC)
        val basicCollection = BasicCollection("BioImages-EMPIAR", "!{S-BIAD}", null, testTime)

        every { queryService.getBasicCollection(parentAccNo) } returns basicCollection
        every { queryService.getAccessTags(parentAccNo) } returns listOf(PUBLIC_ACCESS_TAG.value, parentAccNo)

        val parentInfo = testInstance.getParentInfo(parentAccNo)
        assertThat(parentInfo.accessTags).hasSize(1)
        assertThat(parentInfo.accessTags.first()).isEqualTo(parentAccNo)
        assertThat(parentInfo.releaseTime).isEqualTo(testTime)
        assertThat(parentInfo.parentTemplate).isEqualTo("!{S-BIAD}")
    }

    @Test
    fun `execute collection validators`(
        @MockK validator: CollectionValidator
    ) {
        val testTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, UTC)
        val submission = basicExtSubmission.copy(collections = listOf(ExtCollection("ArrayExpress")))
        val basicProject = BasicCollection("ArrayExpress", "!{E-MTAB}", "ArrayExpressValidator", testTime)

        every { validator.validate(submission) } answers { nothing }
        every { queryService.getBasicCollection("ArrayExpress") } returns basicProject
        every { beanFactory.getBean("ArrayExpressValidator", CollectionValidator::class.java) } returns validator

        testInstance.executeCollectionValidators(submission)

        verify(exactly = 1) { validator.validate(submission) }
    }

    @Test
    fun `execute collection validators over new collection`(
        @MockK validator: CollectionValidator
    ) {
        val submission = basicExtSubmission.copy(
            section = ExtSection(type = "Project"),
            collections = listOf(ExtCollection("ArrayExpress")))

        testInstance.executeCollectionValidators(submission)

        verify(exactly = 0) {
            validator.validate(submission)
            queryService.getBasicCollection("ArrayExpress")
        }
    }

    @Test
    fun `project without access tag`() {
        val parentAccNo = "BioImages-EMPIAR"
        val basicProject = BasicCollection(parentAccNo, "!{S-BIAD}", null, null)

        every { queryService.getBasicCollection(parentAccNo) } returns basicProject
        every { queryService.getAccessTags(parentAccNo) } returns listOf("Empiar")

        assertThrows<CollectionInvalidAccessTagException> { testInstance.getParentInfo(parentAccNo) }
    }
}
