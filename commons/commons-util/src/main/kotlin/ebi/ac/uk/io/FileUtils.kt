package ebi.ac.uk.io

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.streams.toList

@Suppress("TooManyFunctions")
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
        when (source.isDirectory) {
            true -> copyFolder(source.toPath(), target.toPath())
            false -> copyFile(source.toPath(), target.toPath())
        }
    }

    private fun copyFile(source: Path, target: Path) {
        Files.copy(source, createParentDirectories(target), StandardCopyOption.REPLACE_EXISTING)
    }

    private fun copyFolder(source: Path, target: Path) {
        deleteIfExist(target)
        Files.walkFileTree(source, CopyFileVisitor(source, target))
    }

    fun moveFile(source: File, target: File) {
        Files.move(source.toPath(), createParentDirectories(deleteIfExist(target.toPath())))
    }

    fun copyOrReplace(source: File, content: String) {
        Files.write(createParentDirectories(source.toPath()), content.toByteArray())
    }

    fun isDirectory(file: File): Boolean {
        return Files.isDirectory(file.toPath())
    }

    fun listFiles(file: File): List<File> {
        return when (isDirectory(file)) {
            true -> Files.list(file.toPath()).map { it.toFile() }.toList()
            else -> emptyList()
        }
    }

    fun size(file: File): Long {
        return Files.size(file.toPath())
    }

    private fun createParentDirectories(path: Path): Path {
        Files.createDirectories(path.parent)
        return path
    }

    private fun deleteIfExist(path: Path): Path {
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
