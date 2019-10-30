package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.FILE_NAME
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.FILE_REF_NAME
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.assertExtFile
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.fileDb
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.refRileDb
import ebi.ac.uk.io.sources.FilesSource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File as SystemFile

@ExtendWith(MockKExtension::class)
internal class ToExtFileTest(
    @MockK val filesSource: FilesSource,
    @MockK val systemFile: SystemFile
) {
    @Test
    fun `File to ExtFile`() {
        val file = fileDb
        every { filesSource.getFile(file.name) } returns systemFile

        assertExtFile(file.toExtFile(filesSource), systemFile, FILE_NAME)
    }

    @Test
    fun `ReferencedFile to ExtFile`() {
        val refFile = refRileDb
        every { filesSource.getFile(refFile.name) } returns systemFile

        assertExtFile(refFile.toExtFile(filesSource), systemFile, FILE_REF_NAME)
    }
}
