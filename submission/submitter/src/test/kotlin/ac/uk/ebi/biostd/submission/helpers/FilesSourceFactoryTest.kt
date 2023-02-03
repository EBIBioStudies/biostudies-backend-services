package ac.uk.ebi.biostd.submission.helpers

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireClient

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FilesSourceFactoryTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val fireClient: FireClient,
    @MockK private val applicationProperties: ApplicationProperties,
    @MockK private val queryService: SubmissionPersistenceQueryService,
) {
    private val testInstance = FilesSourceFactory(fireClient, applicationProperties, queryService)

    @Test
    fun `submission source`() {
        val refFile = createNfsFile("ref.txt", "Files/ref.txt", tempFolder.createFile("ref.txt"))
        val innerFile = createNfsFile("inner.txt", "Files/inner.txt", tempFolder.createFile("inner.txt"))
        val sub = basicExtSubmission.copy(section = ExtSection(type = "Exp", files = listOf(left(innerFile))))

        every { applicationProperties.submissionPath } returns "sub-path"
        every { queryService.findReferencedFile(sub, "ghost.txt") } returns null
        every { queryService.findReferencedFile(sub, "ref.txt") } returns refFile

        val fileSource = testInstance.createSubmissionSource(sub)

        assertThat(fileSource.getExtFile("ghost.txt")).isNull()
        assertThat(fileSource.getExtFile("ref.txt")).isEqualTo(refFile)
        assertThat(fileSource.getExtFile("inner.txt")).isEqualTo(innerFile)
    }
}
