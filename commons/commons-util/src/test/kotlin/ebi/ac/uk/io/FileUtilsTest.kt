package ebi.ac.uk.io

import ebi.ac.uk.test.clean
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
internal class FileUtilsTest(private val temporaryFolder: TemporaryFolder) {
    @BeforeEach
    fun beforeEach() {
        temporaryFolder.clean()
    }

    @Nested
    inner class MoveFile {

        @Nested
        inner class WhenFile {
            @Test
            fun whenTargetExists() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val another = temporaryFolder.createFile("two.txt", "two")

                FileUtils.moveFile(file, another)

                assertThat(temporaryFolder.root.resolve("one.txt")).doesNotExist()
                assertThat(temporaryFolder.root.resolve("two.txt")).hasContent("one")
            }

            @Test
            fun whenNoTargetExists() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val target = temporaryFolder.root.resolve("new.txt")

                FileUtils.moveFile(file, target)

                assertThat(temporaryFolder.root.resolve("new.txt")).hasContent("one")
            }
        }

        @Nested
        inner class WhenFolder {

            @Test
            fun whenTargetFolderExists() {
                val tempDir = temporaryFolder.createDirectory("directory")
                tempDir.addNewFile("two.txt")

                val targetDirectory = temporaryFolder.createDirectory("directory-target")
                targetDirectory.addNewFile("one.txt")

                FileUtils.moveFile(tempDir, targetDirectory)

                assertThat(targetDirectory).isDirectory()
                assertThat(targetDirectory.list()).containsExactly("two.txt")
            }

            @Test
            fun whenNoTargetFolder() {
                val tempDir = temporaryFolder.createDirectory("directory")
                tempDir.addNewFile("two.txt")
                val target = temporaryFolder.root.resolve("target")

                FileUtils.moveFile(tempDir, target)

                assertThat(target).isDirectory()
                assertThat(target.list()).containsExactly("two.txt")
            }
        }
    }

    @Nested
    inner class DeleteFolder {
        @Test
        fun deleteFolderWhenFileExist() {
            val file = temporaryFolder.createFile("one.txt", "content.exist")

            FileUtils.deleteFolder(file)
            assertThat(file).doesNotExist()
        }

        @Test
        fun deleteFolderWhenNotEmptyFolder() {
            val tempDir = temporaryFolder.createDirectory("dir-example")
            val file = tempDir.resolve("two.txt")
            file.createNewFile()

            FileUtils.deleteFolder(tempDir)
            assertThat(tempDir).doesNotExist()
        }

        @Test
        fun deleteFolderWhenNotExist() {
            val tempDir = temporaryFolder.createDirectory("dir-example")

            FileUtils.deleteFolder(tempDir.resolve("another-dir"))
        }
    }
}
