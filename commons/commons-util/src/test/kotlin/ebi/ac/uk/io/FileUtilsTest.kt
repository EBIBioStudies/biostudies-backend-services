package ebi.ac.uk.io

import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.test.clean
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import java.nio.file.Files.getPosixFilePermissions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
internal class FileUtilsTest(private val temporaryFolder: TemporaryFolder) {
    @BeforeEach
    fun beforeEach() = temporaryFolder.clean()

    @Nested
    inner class CopyOrReplaceFile {
        @Nested
        inner class WhenFile {
            @Test
            fun whenTargetExists() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val another = temporaryFolder.createFile("two.txt", "two")

                FileUtils.copyOrReplaceFile(file, another, RW_______, RWX______)

                assertThat(temporaryFolder.root.resolve("one.txt")).hasContent("one")
                assertThat(temporaryFolder.root.resolve("two.txt")).hasContent("one")
                assertThat(getPosixFilePermissions(another.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
            }

            @Test
            fun whenNoTargetExists() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val target = temporaryFolder.root.resolve("new.txt")

                FileUtils.copyOrReplaceFile(file, target, RW_______, RWX______)

                assertThat(target).hasContent("one")
                assertThat(getPosixFilePermissions(target.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
            }

            @Test
            fun `when there's a nested folder`() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val nested = temporaryFolder.root.resolve("nested")
                val folder = temporaryFolder.root.resolve("nested/folder")
                val target = temporaryFolder.root.resolve("nested/folder/new.txt")

                FileUtils.copyOrReplaceFile(file, target, RW_______, RWX______)

                assertThat(target).hasContent("one")
                assertThat(getPosixFilePermissions(target.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
                assertThat(getPosixFilePermissions(nested.toPath())).containsExactlyInAnyOrderElementsOf(RWX______)
                assertThat(getPosixFilePermissions(folder.toPath())).containsExactlyInAnyOrderElementsOf(RWX______)
            }
        }

        @Nested
        inner class WhenFolder {
            @Test
            fun whenTargetFolderExists() {
                val tempDir = temporaryFolder.createDirectory("directory")
                tempDir.createNewFile("two.txt")

                val targetDirectory = temporaryFolder.createDirectory("target-directory")
                targetDirectory.createNewFile("one.txt")

                FileUtils.copyOrReplaceFile(tempDir, targetDirectory, RW_______, RWX______)

                val folderPermissions = getPosixFilePermissions(targetDirectory.toPath())
                val filePermissions = getPosixFilePermissions(targetDirectory.resolve("two.txt").toPath())
                assertThat(targetDirectory).isDirectory()
                assertThat(targetDirectory.list()).containsExactly("two.txt")
                assertThat(filePermissions).containsExactlyInAnyOrderElementsOf(RW_______)
                assertThat(folderPermissions).containsExactlyInAnyOrderElementsOf(RWX______)
            }

            @Test
            fun whenNoTargetFolder() {
                val tempDir = temporaryFolder.createDirectory("directory")
                tempDir.createNewFile("two.txt")
                val target = temporaryFolder.root.resolve("target")

                FileUtils.copyOrReplaceFile(tempDir, target, RW_______, RWX______)

                val folderPermissions = getPosixFilePermissions(target.toPath())
                val filePermissions = getPosixFilePermissions(target.resolve("two.txt").toPath())
                assertThat(target).isDirectory()
                assertThat(target.list()).containsExactly("two.txt")
                assertThat(filePermissions).containsExactlyInAnyOrderElementsOf(RW_______)
                assertThat(folderPermissions).containsExactlyInAnyOrderElementsOf(RWX______)
            }

            @Test
            fun whenNestedFolder() {
                val tempDir = temporaryFolder.createDirectory("directory")
                val subTempDir = tempDir.createDirectory("subDirectory")
                val subDirFile = subTempDir.createNewFile("subTempFile.txt", "content")
                val target = temporaryFolder.root.resolve("target")
                val nestedDir = temporaryFolder.root.resolve("target/subDirectory")
                val nestedFile = temporaryFolder.root.resolve("target/subDirectory/subTempFile.txt")

                FileUtils.copyOrReplaceFile(tempDir, target, RW_______, RWX______)

                assertThat(target).isDirectory()
                assertThat(nestedDir).isDirectory()
                assertThat(nestedFile).hasSameContentAs(subDirFile)
                assertThat(getPosixFilePermissions(target.toPath())).containsExactlyInAnyOrderElementsOf(RWX______)
                assertThat(getPosixFilePermissions(nestedDir.toPath())).containsExactlyInAnyOrderElementsOf(RWX______)
                assertThat(getPosixFilePermissions(nestedFile.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
            }
        }
    }

    @Nested
    inner class MoveFile {
        @Nested
        inner class WhenFile {
            @Test
            fun whenTargetExists() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val another = temporaryFolder.createFile("two.txt", "two")

                FileUtils.moveFile(file, another, RW_______, RWX______)

                assertThat(temporaryFolder.root.resolve("one.txt")).doesNotExist()
                assertThat(temporaryFolder.root.resolve("two.txt")).hasContent("one")
                assertThat(getPosixFilePermissions(another.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
            }

            @Test
            fun whenNoTargetExists() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val target = temporaryFolder.root.resolve("new.txt")

                FileUtils.moveFile(file, target, RW_______, RWX______)

                assertThat(temporaryFolder.root.resolve("new.txt")).hasContent("one")
                assertThat(getPosixFilePermissions(target.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
            }

            @Test
            fun `when there's a nested folder`() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val nested = temporaryFolder.root.resolve("nested")
                val folder = temporaryFolder.root.resolve("nested/folder")
                val target = temporaryFolder.root.resolve("nested/folder/new.txt")

                FileUtils.moveFile(file, target, RW_______, RWX______)

                assertThat(target).hasContent("one")
                assertThat(getPosixFilePermissions(target.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
                assertThat(getPosixFilePermissions(nested.toPath())).containsExactlyInAnyOrderElementsOf(RWX______)
                assertThat(getPosixFilePermissions(folder.toPath())).containsExactlyInAnyOrderElementsOf(RWX______)
            }
        }

        @Nested
        inner class WhenFolder {
            @Test
            fun whenTargetFolderExists() {
                val tempDir = temporaryFolder.createDirectory("directory")
                tempDir.createNewFile("two.txt")

                val targetDirectory = temporaryFolder.createDirectory("target-directory")
                targetDirectory.createNewFile("one.txt")

                FileUtils.moveFile(tempDir, targetDirectory, RW_______, RWX______)

                val folderPermissions = getPosixFilePermissions(targetDirectory.toPath())
                val filePermissions = getPosixFilePermissions(targetDirectory.resolve("two.txt").toPath())
                assertThat(targetDirectory).isDirectory()
                assertThat(targetDirectory.list()).containsExactly("two.txt")
                assertThat(filePermissions).containsExactlyInAnyOrderElementsOf(RW_______)
                assertThat(folderPermissions).containsExactlyInAnyOrderElementsOf(RWX______)
            }

            @Test
            fun whenNoTargetFolder() {
                val tempDir = temporaryFolder.createDirectory("directory")
                tempDir.createNewFile("two.txt")
                val target = temporaryFolder.root.resolve("target")

                FileUtils.moveFile(tempDir, target, RW_______, RWX______)

                val folderPermissions = getPosixFilePermissions(target.toPath())
                val filePermissions = getPosixFilePermissions(target.resolve("two.txt").toPath())
                assertThat(target).isDirectory()
                assertThat(target.list()).containsExactly("two.txt")
                assertThat(filePermissions).containsExactlyInAnyOrderElementsOf(RW_______)
                assertThat(folderPermissions).containsExactlyInAnyOrderElementsOf(RWX______)
            }
        }
    }

    @Nested
    inner class DeleteFolder {
        @Test
        fun deleteFolderWhenFileExist() {
            val file = temporaryFolder.createFile("one.txt", "content.exist")

            FileUtils.deleteFile(file)
            assertThat(file).doesNotExist()
        }

        @Test
        fun deleteFolderWhenNotEmptyFolder() {
            val tempDir = temporaryFolder.createDirectory("dir-example")
            val file = tempDir.resolve("two.txt")
            file.createNewFile()

            FileUtils.deleteFile(tempDir)
            assertThat(tempDir).doesNotExist()
        }

        @Test
        fun deleteFolderWhenNotExist() {
            val tempDir = temporaryFolder.createDirectory("dir-example")

            FileUtils.deleteFile(tempDir.resolve("another-dir"))
        }
    }
}
