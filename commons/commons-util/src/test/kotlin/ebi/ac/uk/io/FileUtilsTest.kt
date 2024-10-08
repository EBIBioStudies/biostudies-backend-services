package ebi.ac.uk.io

import ebi.ac.uk.io.FileUtilsHelper.createFolderIfNotExist
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.io.ext.newFile
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.test.clean
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Files
import java.nio.file.Files.getPosixFilePermissions
import java.util.concurrent.TimeUnit

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

                FileUtils.copyOrReplaceFile(file, another, Permissions(RW_______, RWX______))

                assertThat(temporaryFolder.root.resolve("one.txt")).hasContent("one")
                assertThat(temporaryFolder.root.resolve("two.txt")).hasContent("one")
                assertThat(getPosixFilePermissions(another.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
            }

            @Test
            fun whenNoTargetExists() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val target = temporaryFolder.root.resolve("new.txt")

                FileUtils.copyOrReplaceFile(file, target, Permissions(RW_______, RWX______))

                assertThat(target).hasContent("one")
                assertThat(getPosixFilePermissions(target.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
            }

            @Test
            fun `when there's a nested folder`() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val nested = temporaryFolder.root.resolve("nested")
                val folder = temporaryFolder.root.resolve("nested/folder")
                val target = temporaryFolder.root.resolve("nested/folder/new.txt")

                FileUtils.copyOrReplaceFile(file, target, Permissions(RW_______, RWX______))

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
                tempDir.newFile("two.txt")

                val targetDirectory = temporaryFolder.createDirectory("target-directory")
                targetDirectory.newFile("one.txt")

                FileUtils.copyOrReplaceFile(tempDir, targetDirectory, Permissions(RW_______, RWX______))

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
                tempDir.newFile("two.txt")
                val target = temporaryFolder.root.resolve("target")

                FileUtils.copyOrReplaceFile(tempDir, target, Permissions(RW_______, RWX______))

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

                FileUtils.copyOrReplaceFile(tempDir, target, Permissions(RW_______, RWX______))

                assertThat(target).isDirectory()
                assertThat(nestedDir).isDirectory()
                assertThat(nestedFile).hasSameTextualContentAs(subDirFile)
                assertThat(getPosixFilePermissions(target.toPath())).containsExactlyInAnyOrderElementsOf(RWX______)
                assertThat(getPosixFilePermissions(nestedDir.toPath())).containsExactlyInAnyOrderElementsOf(RWX______)
                assertThat(getPosixFilePermissions(nestedFile.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
            }
        }
    }

    @Nested
    inner class HardLinks {
        @Test
        fun createHardLinkFile() {
            val permissions = Permissions(RW_______, RWX______)
            val sourceFolder = temporaryFolder.createDirectory("sourceFolder")
            val targetFolder = temporaryFolder.createDirectory("targetFolder")
            val file = sourceFolder.createFile("file.txt")
            val expectedFile = targetFolder.resolve("file.txt")

            FileUtils.createHardLink(file, sourceFolder.toPath(), targetFolder.toPath(), permissions)

            val filesInTarget = FileUtils.listAllFiles(targetFolder)
            assertThat(filesInTarget).containsExactly(expectedFile)
        }

        @Test
        fun createHardLinkFolder() {
            val permissions = Permissions(RW_______, RWX______)
            val sourceFolder = temporaryFolder.createDirectory("sourceFolder")
            val targetFolder = temporaryFolder.createDirectory("targetFolder")
            val folder = sourceFolder.createDirectory("folder")
            val expectedFolder = targetFolder.resolve("folder")

            FileUtils.createHardLink(folder, sourceFolder.toPath(), targetFolder.toPath(), permissions)

            val filesInTarget = FileUtils.listAllFiles(targetFolder)
            assertThat(filesInTarget).containsExactly(expectedFolder)
            assertThat(expectedFolder).isEmptyDirectory()
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

                FileUtils.moveFile(file, another, Permissions(RW_______, RWX______))

                assertThat(temporaryFolder.root.resolve("one.txt")).doesNotExist()
                assertThat(temporaryFolder.root.resolve("two.txt")).hasContent("one")
                assertThat(getPosixFilePermissions(another.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
            }

            @Test
            fun whenNoTargetExists() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val target = temporaryFolder.root.resolve("new.txt")

                FileUtils.moveFile(file, target, Permissions(RW_______, RWX______))

                assertThat(temporaryFolder.root.resolve("new.txt")).hasContent("one")
                assertThat(getPosixFilePermissions(target.toPath())).containsExactlyInAnyOrderElementsOf(RW_______)
            }

            @Test
            fun `when there's a nested folder`() {
                val file = temporaryFolder.createFile("one.txt", "one")
                val nested = temporaryFolder.root.resolve("nested")
                val folder = temporaryFolder.root.resolve("nested/folder")
                val target = temporaryFolder.root.resolve("nested/folder/new.txt")

                FileUtils.moveFile(file, target, Permissions(RW_______, RWX______))

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
                tempDir.newFile("two.txt")

                val targetDirectory = temporaryFolder.createDirectory("target-directory")
                targetDirectory.newFile("one.txt")

                FileUtils.moveFile(tempDir, targetDirectory, Permissions(RW_______, RWX______))

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
                tempDir.newFile("two.txt")
                val target = temporaryFolder.root.resolve("target")

                FileUtils.moveFile(tempDir, target, Permissions(RW_______, RWX______))

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

    @Nested
    inner class DeleteEmptyFolders {
        @Test
        fun whenEmpty() {
            val main = temporaryFolder.createDirectory("main")
            val sub1 = main.createDirectory("sub1")
            val sub2 = main.createDirectory("sub2")
            sub1.createDirectory("sub1Sub1")
            sub1.createDirectory("sub1Sub2")
            sub2.createDirectory("sub2Sub1")

            FileUtils.deleteEmptyDirectories(main)

            assertThat(main).doesNotExist()
        }

        @Test
        fun whenNotEmpty() {
            val main = temporaryFolder.createDirectory("main")
            val sub1 = main.createDirectory("sub1")
            val sub2 = main.createDirectory("sub2")
            val sub1Sub1 = sub1.createDirectory("sub1Sub1")
            val file = sub1Sub1.createFile("one.txt", "text")

            FileUtils.deleteEmptyDirectories(main)

            assertThat(file).exists()
            assertThat(sub1Sub1).exists()
            assertThat(sub2).doesNotExist()
            assertThat(sub1).exists()
            assertThat(main).exists()
        }
    }

    @Nested
    inner class CleanDirectory {
        @Test
        fun `clean directory`() {
            val cleanTempFolder = temporaryFolder.createDirectory("cleanTempFolder")
            val sub1 = cleanTempFolder.createDirectory("sub1")
            val sub2 = cleanTempFolder.createDirectory("sub2")
            val sub1Sub1 = sub1.createDirectory("sub1Sub1")
            val file = sub1Sub1.createFile("one.txt", "text")

            FileUtils.cleanDirectory(cleanTempFolder)

            assertThat(cleanTempFolder).exists()
            assertThat(sub1).doesNotExist()
            assertThat(sub2).doesNotExist()
            assertThat(sub1Sub1).doesNotExist()
            assertThat(file).doesNotExist()
        }
    }

    @Nested
    inner class Utilities {
        @Test
        fun `is directory`() {
            val file = temporaryFolder.createFile("test.txt")
            val folder = temporaryFolder.createDirectory("test-folder")

            assertThat(FileUtils.isDirectory(file)).isFalse
            assertThat(FileUtils.isDirectory(folder)).isTrue
        }

        @Test
        fun `file size`() {
            val file = temporaryFolder.createFile("size-test.txt", "a test text")
            assertThat(FileUtils.size(file)).isEqualTo(11L)
        }

        @Test
        fun `directory size with calculateDirectories enabled`() {
            val directory = temporaryFolder.createDirectory("dir-size-test")

            temporaryFolder.createDirectory("dir-size-test/level-1")
            temporaryFolder.createDirectory("dir-size-test/level-1/level-2")
            val file1 = temporaryFolder.createFile("dir-size-test/file1.txt", "test 1")
            val file2 = temporaryFolder.createFile("dir-size-test/level-1/file2.txt", "test 2")
            val file3 = temporaryFolder.createFile("dir-size-test/level-1/level-2/file3.txt", "test 3")

            assertThat(FileUtils.size(directory)).isEqualTo(file1.size() + file2.size() + file3.size())
        }

        @Test
        fun `directory size with calculateDirectories disabled`() {
            val directory = temporaryFolder.createDirectory("dir-size-test")

            temporaryFolder.createDirectory("dir-size-test/level-1")
            temporaryFolder.createDirectory("dir-size-test/level-1/level-2")
            temporaryFolder.createFile("dir-size-test/file1.txt", "test 1")
            temporaryFolder.createFile("dir-size-test/level-1/file2.txt", "test 2")
            temporaryFolder.createFile("dir-size-test/level-1/level-2/file3.txt", "test 3")

            assertThat(FileUtils.size(directory, iterateDirectories = false)).isEqualTo(Files.size(directory.toPath()))
        }

        @Test
        fun md5() {
            val folder = temporaryFolder.createDirectory("md5-test")
            val file = temporaryFolder.createFile("md5-test.txt", "a file for md5 test")

            assertThat(FileUtils.md5(folder)).isEmpty()
            assertThat(FileUtils.md5(file)).isEqualTo("FC5D029EE5D34A268F8FA016E949073B")
        }

        @Test
        fun listAllFiles() {
            val folder = temporaryFolder.createDirectory("list-all-test")
            val dir1 = folder.createDirectory("dir1")
            val dir2 = folder.createDirectory("dir2")
            val file3 = folder.createFile("file3", "file3")
            val dir11 = dir1.createDirectory("dir11")
            val file11a = dir11.createFile("file11a", "11a-content")
            val file11b = dir11.createFile("file11b", "11b-content")

            val files = FileUtils.listAllFiles(folder)
            assertThat(files).containsExactly(dir1, dir11, file11a, file11b, dir2, file3)
        }

        @Nested
        inner class Md5Performance {
            lateinit var file: File

            @BeforeEach
            fun beforeEach() {
                val oneGb = (1024 * 1024 * 1024).toLong()
                file = temporaryFolder.createFile("md5-p-test.txt", oneGb)
            }

            @Test
            @Timeout(10_000, unit = TimeUnit.MILLISECONDS)
            fun md5Performance() {
                assertThat(FileUtils.md5(file)).isNotEmpty()
            }
        }

        @Test
        fun `list files`() {
            val folder = temporaryFolder.createDirectory("listing-test")
            val file1 = temporaryFolder.createFile("listing-test/listing-file1.txt")
            val file2 = temporaryFolder.createFile("listing-test/listing-file2.txt")
            val innerFolder = temporaryFolder.createDirectory("listing-test/inner-folder")

            assertThat(FileUtils.listFiles(file1)).isEmpty()
            assertThat(FileUtils.listFiles(folder)).containsExactlyInAnyOrder(innerFolder, file1, file2)
        }
    }

    @Nested
    inner class FileUtilsHelperTest {
        @Test
        fun `create folder if not exists when not exists`() {
            val folder = temporaryFolder.root.resolve("folder1")
            assertThat(folder).doesNotExist()

            createFolderIfNotExist(folder.toPath(), RW_______)

            assertThat(folder).exists()
            assertThat(getPosixFilePermissions(folder.toPath())).isEqualTo(RW_______)
        }

        @Test
        fun `create folder if not exists when exists`() {
            val folder = temporaryFolder.createDirectory("folder2")
            Files.setPosixFilePermissions(folder.toPath(), RW_______)
            assertThat(folder).exists()

            createFolderIfNotExist(folder.toPath(), RWXRWX___)

            assertThat(folder).exists()
            assertThat(getPosixFilePermissions(folder.toPath())).isEqualTo(RW_______)
        }
    }
}
