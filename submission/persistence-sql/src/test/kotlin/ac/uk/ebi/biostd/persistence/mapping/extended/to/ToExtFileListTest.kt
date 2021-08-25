package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.refRileDb
import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ac.uk.ebi.biostd.persistence.model.ReferencedFileList
import ebi.ac.uk.io.sources.FilesSource
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class ToExtFileListTest(
    temporaryFolder: TemporaryFolder,
    @MockK val fileSource: FilesSource
) {
    private var file: DbReferencedFile = refRileDb
    private val systemFile = temporaryFolder.createFile("test.txt")
    private val anotherSystemFile = temporaryFolder.createFile("test2.txt")
    private val fileList = ReferencedFileList("fileList", sortedSetOf(file))
}
