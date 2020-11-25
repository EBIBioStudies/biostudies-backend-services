package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.model.BasicProject
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
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
class ParentInfoServiceTest(
    @MockK private val queryService: SubmissionMetaQueryService
) {
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
        val basicProject = BasicProject("BioImages-EMPIAR", "!{S-BIAD}", testTime)

        every { queryService.getBasicProject(parentAccNo) } returns basicProject
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
        val basicProject = BasicProject(parentAccNo, "!{S-BIAD}", null)

        every { queryService.getBasicProject(parentAccNo) } returns basicProject
        every { queryService.getAccessTags(parentAccNo) } returns listOf("Empiar")

        assertThrows<ProjectInvalidAccessTagException> { testInstance.getParentInfo(parentAccNo) }
    }
}
