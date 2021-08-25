package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.FILE_NAME
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.FILE_REF_NAME
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.assertExtFile
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.fileDb
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.refRileDb
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.NfsBioFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class ToExtFileTest(
    tempFolder: TemporaryFolder,
    @MockK val filesSource: FilesSource,
) {
    private val bioFile = NfsBioFile(tempFolder.createFile(fileDb.name))

    @Test
    fun `File to ExtFile`() {
        val dbFile = fileDb
        every { filesSource.getFile(dbFile.name) } returns bioFile

        assertExtFile(dbFile.toExtFile(filesSource), bioFile, FILE_NAME)
    }

    @Test
    fun `ReferencedFile to ExtFile`() {
        val refFile = refRileDb
        every { filesSource.getFile(refFile.name) } returns bioFile

        assertExtFile(refFile.toExtFile(filesSource), bioFile, FILE_REF_NAME)
    }
}
