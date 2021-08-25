package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.assertExtFile
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.fileDb
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.assertj.core.api.Assertions.assertThat as assertThat1

private const val FILE_1 = "file1"
private const val FILE_2 = "file2"
private const val FILE_3 = "file3"

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class ToExtFilesTest(
    @MockK private val filesSource: FilesSource,
    tempFolder: TemporaryFolder
) {
    private val file = fileDb.apply { name = FILE_1; order = 0; tableIndex = NO_TABLE_INDEX }
    private val tableFile = fileDb.apply { name = FILE_2; order = 1; tableIndex = 0; }
    private val anotherTableFile = fileDb.apply { name = FILE_3; order = 2; tableIndex = 1; }

    private val files = sortedSetOf(file, tableFile, anotherTableFile)
    private val systemFile1 = NfsBioFile(tempFolder.createFile(FILE_1))
    private val systemFile2 = NfsBioFile(tempFolder.createFile(FILE_2))
    private val systemFile3 = NfsBioFile(tempFolder.createFile(FILE_3))

    @Test
    fun `Files to ExtFileTable`() {
        every { filesSource.getFile(FILE_1) } returns systemFile1
        every { filesSource.getFile(FILE_2) } returns systemFile2
        every { filesSource.getFile(FILE_3) } returns systemFile3

        val files = files.toExtFiles(filesSource)

        assertThat(files.first()).hasLeftValueSatisfying { assertExtFile(it, systemFile1, FILE_1) }
        assertThat(files.second()).hasRightValueSatisfying { table ->
            assertThat1(table.files).hasSize(2)
            assertExtFile(table.files.first(), systemFile2, FILE_2)
            assertExtFile(table.files.second(), systemFile3, FILE_3)
        }
    }
}
