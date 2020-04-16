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
internal class NfsFileUtilsTest(private val temporaryFolder: TemporaryFolder) {
    @BeforeEach
    fun beforeEach() {
        temporaryFolder.clean()
    }

    @Nested
    inner class MoveFile {

        @Nested
        inner class WhenFile {
            @Test
            fun whenExistTarget() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val another = temporaryFolder.createFile("two.txt", "two")

                NfsFileUtils.moveFile(file, another)

                assertThat(temporaryFolder.root.resolve("one.txt")).doesNotExist()
                assertThat(temporaryFolder.root.resolve("two.txt")).hasContent("one")
            }

            @Test
            fun whenNotExistTarget() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val target = temporaryFolder.root.resolve("new.txt")

                NfsFileUtils.moveFile(file, target)

                assertThat(temporaryFolder.root.resolve("new.txt")).hasContent("one")
            }
        }

        @Nested
        inner class WhenFolder {

            @Test
            fun whenTargetFolderExist() {
                val tempDir = temporaryFolder.createDirectory("directory")
                tempDir.addNewFile("two.txt")

                val targetDirectory = temporaryFolder.createDirectory("directory-target")
                targetDirectory.addNewFile("one.txt")

                NfsFileUtils.moveFile(tempDir, targetDirectory)

                assertThat(targetDirectory).isDirectory()
                assertThat(targetDirectory.list()).containsExactly("two.txt")
            }

            @Test
            fun whenNoTargetFolder() {
                val tempDir = temporaryFolder.createDirectory("directory")
                tempDir.addNewFile("two.txt")
                val target = temporaryFolder.root.resolve("target")

                NfsFileUtils.moveFile(tempDir, target)

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

            NfsFileUtils.deleteFolder(file)
            assertThat(file).doesNotExist()
        }

        @Test
        fun deleteFolderWhenNotEmptyFolder() {
            val tempDir = temporaryFolder.createDirectory("dir-example")
            val file = tempDir.resolve("two.txt")
            file.createNewFile()

            NfsFileUtils.deleteFolder(tempDir)
            assertThat(tempDir).doesNotExist()
        }

        @Test
        fun deleteFolderWhenNotExist() {
            val tempDir = temporaryFolder.createDirectory("dir-example")

            NfsFileUtils.deleteFolder(tempDir.resolve("another-dir"))
        }
    }
}
