package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.processors.IProjectProcessor
import ac.uk.ebi.biostd.submission.test.createBasicProject
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.model.constants.Processed
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ProjectSubmitterTest(
    @MockK private val accNoPatternUtil: AccNoPatternUtil,
    @MockK private val projectProcessor: IProjectProcessor,
    @MockK private val persistenceContext: PersistenceContext
) {
    private val project = createBasicProject()
    private val testInstance = ProjectSubmitter(accNoPatternUtil, listOf(projectProcessor))

    @BeforeEach
    fun beforeEach() {
        every { accNoPatternUtil.getPattern("!{S-ABC}") } returns "S-ABC"
        every { persistenceContext.saveAccessTag("ABC456") } answers { nothing }
        every { persistenceContext.saveSubmission(project) } answers { project }
        every { projectProcessor.process(project, persistenceContext) } answers { nothing }
        every { persistenceContext.createAccNoPatternSequence("S-ABC") } answers { nothing }
    }

    @Test
    fun submit() {
        testInstance.submit(project, persistenceContext)

        assertThat(project.processingStatus).isEqualTo(Processed)
        verify(exactly = 1) {
            accNoPatternUtil.getPattern("!{S-ABC}")
            persistenceContext.saveAccessTag("ABC456")
            persistenceContext.saveSubmission(project)
            persistenceContext.createAccNoPatternSequence("S-ABC")

            projectProcessor.process(project, persistenceContext)
        }
    }
}
