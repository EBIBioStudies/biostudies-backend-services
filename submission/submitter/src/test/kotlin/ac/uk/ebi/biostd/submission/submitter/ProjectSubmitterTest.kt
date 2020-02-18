package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.service.ProjectValidationService
import ac.uk.ebi.biostd.submission.service.TimesService
import ac.uk.ebi.biostd.submission.test.createBasicProject
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@Disabled
@ExtendWith(MockKExtension::class)
class ProjectSubmitterTest(
    @MockK private val timesService: TimesService,
    @MockK private val accNoPatternUtil: AccNoPatternUtil,
    @MockK private val persistenceContext: PersistenceContext,
    @MockK private val projectValidationService: ProjectValidationService
) {
    private val project = createBasicProject()
    private val testInstance =
        ProjectSubmitter(timesService, accNoPatternUtil, persistenceContext, projectValidationService)

    @BeforeEach
    fun beforeEach() {
        every { accNoPatternUtil.getPattern("!{S-ABC}") } returns "S-ABC"
        every { persistenceContext.saveAccessTag("ABC456") } answers { nothing }
        every { persistenceContext.saveSubmission(project) } answers { project }
        every { projectValidationService.validate(project) } answers { nothing }
        every { persistenceContext.createAccNoPatternSequence("S-ABC") } answers { nothing }
    }

    @Test
    fun submit() {
        testInstance.submit(project)

        assertThat(project.processingStatus).isEqualTo(PROCESSED)
        verify(exactly = 1) {
            accNoPatternUtil.getPattern("!{S-ABC}")
            projectValidationService.validate(project)
            persistenceContext.saveSubmission(project)
            persistenceContext.saveAccessTag("ABC456")
            persistenceContext.createAccNoPatternSequence("S-ABC")
        }
    }
}
