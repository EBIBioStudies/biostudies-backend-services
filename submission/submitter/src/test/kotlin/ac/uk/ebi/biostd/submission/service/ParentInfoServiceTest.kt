package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.exception.ProjectNotFoundException
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.submission.exceptions.ProjectInvalidAccessTagException
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class ParentInfoServiceTest(@MockK private val queryService: SubmissionQueryService) {
    private val testInstance = ParentInfoService(queryService)

    @Test
    fun `no parent`() {
        val parentInfo = testInstance.getParentInfo(null)
        assertThat(parentInfo.accessTags).isEmpty()
        assertThat(parentInfo.releaseTime).isNull()
        assertThat(parentInfo.parentTemplate).isNull()
    }

    @Test
    fun `get parent info`() {
        val parentTemplate = "!{S-BIAD}"
        val parentAccNo = "BioImages-EMPIAR"
        val testTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC)

        every { queryService.existByAccNo(parentAccNo) } returns true
        every { queryService.getReleaseTime(parentAccNo) } returns testTime
        every { queryService.getParentAccPattern(parentAccNo) } returns parentTemplate
        every { queryService.getAccessTags(parentAccNo) } returns listOf(PUBLIC_ACCESS_TAG.value, parentAccNo)

        val parentInfo = testInstance.getParentInfo(parentAccNo)
        assertThat(parentInfo.accessTags).hasSize(1)
        assertThat(parentInfo.accessTags.first()).isEqualTo(parentAccNo)
        assertThat(parentInfo.releaseTime).isEqualTo(testTime)
        assertThat(parentInfo.parentTemplate).isEqualTo(parentTemplate)
    }

    @Test
    fun `project without access tag`() {
        val parentAccNo = "BioImages-EMPIAR"
        every { queryService.existByAccNo(parentAccNo) } returns true
        every { queryService.getAccessTags(parentAccNo) } returns listOf("Empiar")

        assertThrows<ProjectInvalidAccessTagException> { testInstance.getParentInfo(parentAccNo) }
    }

    @Test
    fun `project not existing`() {
        val parentAccNo = "BioImages-EMPIAR"
        every { queryService.existByAccNo(parentAccNo) } returns false

        assertThrows<ProjectNotFoundException> { testInstance.getParentInfo(parentAccNo) }
    }
}
