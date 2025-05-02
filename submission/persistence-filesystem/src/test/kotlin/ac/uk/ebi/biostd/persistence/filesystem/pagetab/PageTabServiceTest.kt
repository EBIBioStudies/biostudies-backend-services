package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.base.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtLinkList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.test.basicExtSubmission
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class PageTabServiceTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val pageTabUtil: PageTabUtil,
) {
    private val baseTempDir = temporaryFolder.createDirectory("files-temp-dir")
    private val subJson = temporaryFolder.createFile("sub.json")
    private val subTsv = temporaryFolder.createFile("sub.tsv")

    private val fileListJson = temporaryFolder.createFile("fileList.json")
    private val fileListTsv = temporaryFolder.createFile("fileList.tsv")

    private val linkListJson = temporaryFolder.createFile("linkList.json")
    private val linkListTsv = temporaryFolder.createFile("linkList.tsv")

    private val testInstance = PageTabService(baseTempDir, pageTabUtil)

    @Test
    fun `generate pagetab`() =
        runTest {
            val tempDir = File("${baseTempDir.absolutePath}/S-TEST123/1")
            val fileList = ExtFileList(filePath = "a-path", temporaryFolder.createFile("file-list"))
            val linkList = ExtLinkList(filePath = "b-path", temporaryFolder.createFile("link-list"))
            val section = ExtSection(type = "t2", fileList = fileList, linkList = linkList)
            val rootSection = ExtSection(type = "t1", sections = listOf(Either.left(section)))
            val sub = basicExtSubmission.copy(section = rootSection)
            val fileListPageTab = mapOf("a-path" to PageTabFiles(fileListJson, fileListTsv))
            val linkListPageTab = mapOf("b-path" to PageTabFiles(linkListJson, linkListTsv))

            coEvery { pageTabUtil.generateSubPageTab(sub, tempDir) } returns PageTabFiles(subJson, subTsv)
            coEvery { pageTabUtil.generateFileListPageTab(sub, tempDir) } returns fileListPageTab
            coEvery { pageTabUtil.generateLinkListPageTab(sub, tempDir) } returns linkListPageTab

            val result = testInstance.generatePageTab(sub)

            assertThat(result.pageTabFiles).hasSize(2)
            assertFile(result.pageTabFiles.first(), subJson, "S-TEST123.json")
            assertFile(result.pageTabFiles.second(), subTsv, "S-TEST123.tsv")
            assertThat(result.section.sections.first()).hasLeftValueSatisfying {
                val resultFileList = it.fileList
                assertThat(resultFileList).isNotNull()
                assertFile(it.fileList?.pageTabFiles?.first(), fileListJson, "Files/a-path.json")
                assertFile(it.fileList?.pageTabFiles?.second(), fileListTsv, "Files/a-path.tsv")

                val resultLinkList = it.linkList
                assertThat(resultLinkList).isNotNull()
                assertFile(it.linkList?.pageTabFiles?.first(), linkListJson, "Files/b-path.json")
                assertFile(it.linkList?.pageTabFiles?.second(), linkListTsv, "Files/b-path.tsv")
            }
        }

    private fun assertFile(
        file: ExtFile?,
        expected: File,
        relPath: String,
    ) {
        assertThat(file).isInstanceOf(NfsFile::class.java)
        val nfsFile = file as NfsFile
        assertThat(nfsFile.file).isEqualTo(expected)
        assertThat(nfsFile.relPath).isEqualTo(relPath)
        assertThat(nfsFile.fullPath).isEqualTo(expected.absolutePath)
        assertThat(nfsFile.attributes).isEmpty()
        assertThat(nfsFile.size).isEqualTo(expected.size())
        assertThat(nfsFile.md5).isEqualTo(expected.md5())
    }
}
