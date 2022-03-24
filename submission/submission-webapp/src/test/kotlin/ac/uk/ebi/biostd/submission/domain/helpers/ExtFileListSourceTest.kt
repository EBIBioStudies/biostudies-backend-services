package ac.uk.ebi.biostd.submission.domain.helpers

import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.io.sources.FireBioFile
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireWebClient

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class ExtFileListSourceTest(
    tempFolder: TemporaryFolder,
    @MockK private val fireWebClient: FireWebClient
) {

    private val nfsFilename = "nfsFile.txt"
    private val fireFilename = "fireFile.txt"
    private val nfsFile = tempFolder.createFile(nfsFilename)
    private val fireFile = tempFolder.createFile(fireFilename, "the content")

    private val files: List<ExtFile> = listOf(
        createNfsFile("folder/$nfsFilename", "Files/folder/$nfsFilename", nfsFile),
        FireFile(
            "folder/$fireFilename",
            "Files/folder/$fireFilename",
            "fireId",
            fireFile.md5(),
            fireFile.size(),
            listOf()
        )
    )

    private val testInstance: ExtFileListSource = ExtFileListSource(fireWebClient, files)

    @Test
    fun `getFile when nfsBioFile`() {
        val result = testInstance.getFile(nfsFilename)

        assertThat(result is NfsBioFile) { "expecting result to be of type NfsBioFile" }
        assertThat(result.file).isEqualTo(nfsFile)
    }

    @Test
    fun `getFile when fireBioFile`() {
        every { fireWebClient.downloadByFireId("fireId", fireFilename) } returns fireFile

        val result = testInstance.getFile(fireFilename)

        assertThat(result is FireBioFile) { "expecting result to be of type NfsBioFile" }
        assertThat(result.fireId).isEqualTo("fireId")
        assertThat(result.fileName).isEqualTo("fireFile.txt")
        assertThat(result.md5).isEqualTo(fireFile.md5())
        assertThat(result.size).isEqualTo(fireFile.size())
        assertThat(result.readContent()).isEqualTo(fireFile.readText())
    }

    @Test
    fun `get non existing file`() {
        assertThat(testInstance.getFile("ghost.txt")).isNull()
    }
}
