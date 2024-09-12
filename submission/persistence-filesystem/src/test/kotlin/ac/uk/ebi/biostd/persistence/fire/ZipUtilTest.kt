package ac.uk.ebi.biostd.persistence.fire

import ac.uk.ebi.biostd.persistence.filesystem.fire.ZipUtil
import ebi.ac.uk.exception.CorruptedFileException
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.createOrReplaceFile
import ebi.ac.uk.test.clean
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ZipUtilTest(
    private val tempFolder: TemporaryFolder
) {
    @BeforeEach
    fun beforeEach() {
        tempFolder.clean()
    }

    @Test
    fun `check zip integrity with different file content`() {
        val source = tempFolder.createDirectory("integrity")
        val innerFolder = source.createDirectory("inner-integrity")
        source.createFile("integrity-test-1.txt", "integrity 1")
        innerFolder.createFile("integrity-test-2.txt", "integrity 2")
        val invalidZip = tempFolder.createFile("invalid.zip")
        ZipUtil.pack(source, invalidZip)
        val invalidFile = innerFolder.createOrReplaceFile("integrity-test-2.txt", "integrity 2 modified")

        val error = assertThrows<CorruptedFileException> { ZipUtil.checkZipIntegrity(source, invalidZip) }

        assertThat(error.message).isEqualTo("The file ${invalidFile.absolutePath} doesn't match the expected MD5")
    }

    @Test
    fun `check zip integrity missing file`() {
        val source = tempFolder.createDirectory("integrity")
        val innerFolder = source.createDirectory("inner-integrity")
        source.createFile("integrity-test-1.txt", "integrity 1")
        val invalidZip = tempFolder.createFile("invalid.zip")
        ZipUtil.pack(source, invalidZip)
        val invalidFile = innerFolder.createFile("integrity-test-2.txt", "integrity 2")

        val error = assertThrows<CorruptedFileException> { ZipUtil.checkZipIntegrity(source, invalidZip) }

        assertThat(error.message).isEqualTo("The file ${invalidFile.absolutePath} doesn't match the expected MD5")
    }
}
