package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.FireDocDirectory
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileRef
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.fireDocFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.nfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.assertExtSection
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.docSection
import ac.uk.ebi.biostd.persistence.doc.test.TEST_FILENAME
import arrow.core.Either.Companion.left
import org.assertj.core.api.Assertions.assertThat
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ToExtSectionTest(temporaryFolder: TemporaryFolder) {
    private val testFile = temporaryFolder.createFile(TEST_FILENAME)
    private val testNfsDocFile = nfsDocFile.copy(fullPath = testFile.absolutePath)
    private val testFireDocFile = fireDocFile

    private val tabFireFile =
        FireDocFile("fileName", "folder/fileName", "Files/folder/fileName", "fireId", listOf(), "md5", 1)
    private val tabFireDirectory =
        FireDocDirectory("fileName", "folder/fileName", "Files/folder/fileName", listOf(), "md5", 2)
    private val fileNfs = temporaryFolder.createFile("fileNfs.txt")
    private val tabNfsFile =
        NfsDocFile(
            fileNfs.name,
            "folder/${fileNfs.name}",
            "Files/folder/${fileNfs.name}",
            fileNfs.absolutePath,
            listOf(),
            fileNfs.md5(),
            fileNfs.size(),
            "fileType"
        )

    private val testDocSection = docSection.copy(
        files = listOf(left(testNfsDocFile), left(testFireDocFile)),
        fileList = docFileList.copy(
            files = listOf(docFileRef),
            pageTabFiles = listOf(tabFireFile, tabFireDirectory, tabNfsFile)
        )
    )

    @Test
    fun `to ext section`() {
        val extSection = testDocSection.toExtSection()
        assertExtSection(extSection, testFile)
        assertFileListTabFiles(extSection.fileList!!.pageTabFiles)
    }

    private fun assertFileListTabFiles(pageTabFiles: List<ExtFile>) {
        assertThat(pageTabFiles.first()).isEqualTo(
            FireFile(
                tabFireFile.filePath,
                tabFireFile.relPath,
                tabFireFile.fireId,
                tabFireFile.md5,
                tabFireFile.fileSize,
                listOf()
            )
        )
        assertThat(pageTabFiles.first().fileName).isEqualTo(tabFireFile.filePath.substringAfterLast("/"))
        assertThat(pageTabFiles.second()).isEqualTo(
            FireDirectory(
                tabFireDirectory.filePath,
                tabFireDirectory.relPath,
                tabFireDirectory.md5,
                tabFireDirectory.fileSize,
                listOf()
            )
        )
        assertThat(pageTabFiles.second().fileName).isEqualTo(tabFireDirectory.filePath.substringAfterLast("/"))
        assertThat(pageTabFiles.third()).isEqualTo(
            NfsFile(
                "folder/${fileNfs.name}",
                "Files/folder/${fileNfs.name}",
                fileNfs.absolutePath,
                fileNfs,
                listOf()
            )
        )
        assertThat(pageTabFiles.third().fileName).isEqualTo(fileNfs.name)
    }
}
