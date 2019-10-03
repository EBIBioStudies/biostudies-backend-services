package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ProjectProcessorTest(@MockK private val persistenceContext: PersistenceContext) {
    private val testInstance = ProjectProcessor()

    @Test
    fun process() {
        val project = createBasicExtendedSubmission()
        testInstance.process(project, persistenceContext)

        assertThat(project.accessTags).hasSize(1)
        assertThat(project.accessTags.first()).isEqualTo("ABC456")
    }
}
