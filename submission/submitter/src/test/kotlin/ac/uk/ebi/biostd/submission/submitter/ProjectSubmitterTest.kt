package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.processors.IProjectProcessor
import ac.uk.ebi.biostd.submission.test.createBasicProject
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ac.uk.ebi.biostd.submission.validators.IProjectValidator
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ProjectSubmitterTest(
    @MockK private val accNoPatternUtil: AccNoPatternUtil,
    @MockK private val projectValidator: IProjectValidator,
    @MockK private val projectProcessor: IProjectProcessor,
    @MockK private val persistenceContext: PersistenceContext
) {
    private val project = createBasicProject()
    private val testInstance = ProjectSubmitter(accNoPatternUtil, listOf(projectValidator), listOf(projectProcessor))

    @BeforeEach
    fun beforeEach() {
        every { persistenceContext.saveAccessTag("ABC456") } answers { nothing }
        every { persistenceContext.saveSubmission(project) } answers { nothing }
        every { accNoPatternUtil.getPattern("!{S-ABC}") } returns AccPattern("S-ABC")
        every { projectProcessor.process(project, persistenceContext) } answers { nothing }
        every { projectValidator.validate(project, persistenceContext) } answers { nothing }
        every { persistenceContext.createAccNoPatternSequence(AccPattern("S-ABC")) } answers { nothing }
    }

    @Test
    fun submit() {
        testInstance.submit(project, persistenceContext)

        verify(exactly = 1) {
            accNoPatternUtil.getPattern("!{S-ABC}")
            persistenceContext.saveAccessTag("ABC456")
            persistenceContext.saveSubmission(project)
            persistenceContext.createAccNoPatternSequence(AccPattern("S-ABC"))

            projectProcessor.process(project, persistenceContext)
            projectValidator.validate(project, persistenceContext)
        }
    }
}
