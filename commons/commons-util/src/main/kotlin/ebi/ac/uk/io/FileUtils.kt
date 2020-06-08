package ebi.ac.uk.io

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import kotlin.streams.toList

@Suppress("TooManyFunctions")
// TODO: merge with #PermissionFileUtils
object FileUtils {
    fun deleteFolder(file: File) {
        deleteFolder(file.toPath())
    }

    fun reCreateDirectory(file: File): File {
        val path = file.toPath()
        deleteFolder(path)
        Files.createDirectory(path)
        return file
    }

    fun copyOrReplaceFile(source: File, target: File) {
        when (isDirectory(source)) {
            true -> copyFolder(source.toPath(), target.toPath())
            false -> copyFile(source.toPath(), target.toPath())
        }
    }

    fun createHardLink(source: File, target: File) {
        when (isDirectory(source)) {
            true -> createFolderHardLinks(source.toPath(), target.toPath())
            false -> createFileHardLink(source.toPath(), target.toPath())
        }
    }

    fun moveFile(source: File, target: File) {
        Files.move(source.toPath(), createParentDirectories(deleteIfExist(target.toPath())))
    }

    fun copyOrReplace(source: File, content: String) {
        Files.write(createParentDirectories(source.toPath()), content.toByteArray())
    }

    fun isDirectory(file: File): Boolean = Files.isDirectory(file.toPath())

    fun listFiles(file: File): List<File> {
        return when (isDirectory(file)) {
            true -> Files.list(file.toPath()).map { it.toFile() }.toList()
            else -> emptyList()
        }
    }

    fun size(file: File): Long = Files.size(file.toPath())

    private fun copyFolder(source: Path, target: Path) {
        deleteIfExist(target)
        Files.walkFileTree(source, CopyFileVisitor(source, target))
    }

    private fun copyFile(source: Path, target: Path) {
        Files.copy(source, createParentDirectories(target), StandardCopyOption.REPLACE_EXISTING)
    }

    private fun createFileHardLink(source: Path, target: Path) {
        deleteIfExist(target)
        Files.createLink(source, createParentDirectories(target))
    }

    private fun createFolderHardLinks(source: Path, target: Path) {
        deleteIfExist(target)
        Files.walkFileTree(source, HardLinkFileVisitor(source, target))
    }

    private fun createParentDirectories(path: Path): Path {
        PosixFilePermissions.fromString("rwxr-x---")
        Files.createDirectories(path.parent)
        return path
    }

    internal fun deleteIfExist(path: Path): Path {
        deleteFolder(path)
        return path
    }

    private fun deleteFolder(path: Path) {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }
    }
}
