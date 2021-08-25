package ebi.ac.uk.io.sources

import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.errors.FileNotFoundException
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
internal class BioListFilesSourceTest(tempFolder: TemporaryFolder) {

    private val nfsFilename = "nfsFile.txt"
    private val fireFilename = "fireFile.txt"
    private val nfsFile = tempFolder.createFile(nfsFilename)

    private val files: List<ExtFile> = listOf(
        NfsFile(nfsFilename, nfsFile),
        FireFile(fireFilename, "fireId", "md5", 1, listOf())
    )

    private val testInstance: BioListFilesSource = BioListFilesSource(files)

    @Test
    fun exists() {
        assertThat(testInstance.exists(nfsFilename)).isTrue
        assertThat(testInstance.exists(fireFilename)).isTrue
    }

    @Test
    fun `exists when don't exist`() {
        assertThat(testInstance.exists("ghost.txt")).isFalse
    }

    @Test
    fun `getFile when nfsBioFile`() {
        val result = testInstance.getFile(nfsFilename)

        assertThat(result is NfsBioFile) { "expecting result to be of type NfsBioFile" }
        assertThat(result.file).isEqualTo(nfsFile)
    }

    @Test
    fun `getFile when fireBioFile`() {
        val result = testInstance.getFile(fireFilename)

        assertThat(result is FireBioFile) { "expecting result to be of type NfsBioFile" }
        assertThat(result.fireId).isEqualTo("fireId")
        assertThat(result.md5).isEqualTo("md5")
    }

    @Test
    fun `get non existing file`() {
        val exception = assertThrows<FileNotFoundException> { testInstance.getFile("ghost.txt") }
        assertThat(exception.message).isEqualTo("File not found: ghost.txt")
    }
}
