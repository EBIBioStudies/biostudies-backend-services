package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.assertFileDb
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.refRileDb
import ac.uk.ebi.biostd.persistence.model.ReferencedFile
import ac.uk.ebi.biostd.persistence.model.ReferencedFileList
import ebi.ac.uk.io.sources.FilesSource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File as SystemFile

@ExtendWith(MockKExtension::class)
internal class ToExtFileListTest(
    @MockK val fileSource: FilesSource,
    @MockK val systemFile: SystemFile,
    @MockK val anotherSystemFile: SystemFile
) {

    private var file: ReferencedFile = refRileDb
    private val fileList = ReferencedFileList("fileList", sortedSetOf(file))

    @Test
    fun toExtFileList() {
        every { fileSource.getFile(fileList.name) } returns systemFile
        every { fileSource.getFile(file.name) } returns anotherSystemFile

        val extFileList = fileList.toExtFileList(fileSource)
        assertThat(extFileList.fileName).isEqualTo(fileList.name)
        assertThat(extFileList.file).isEqualTo(systemFile)

        assertThat(extFileList.files).hasSize(1)
        assertFileDb(extFileList.files.first(), anotherSystemFile)
    }
}
