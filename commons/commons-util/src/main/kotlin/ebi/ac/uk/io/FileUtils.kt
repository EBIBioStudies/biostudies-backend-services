package ebi.ac.uk.io

import ebi.ac.uk.io.FileUtilsHelper.createFileHardLink
import ebi.ac.uk.io.FileUtilsHelper.createFolderHardLinks
import ebi.ac.uk.io.FileUtilsHelper.createFolderIfNotExist
import ebi.ac.uk.io.FileUtilsHelper.createParentDirectories
import java.io.File
import java.nio.file.Files
import java.nio.file.Files.exists
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.attribute.PosixFilePermissions.asFileAttribute
import kotlin.streams.toList

val ONLY_USER: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwx------")

@Suppress("TooManyFunctions")
object FileUtils {
    fun copyOrReplaceFile(
        source: File,
        target: File,
        permissions: Set<PosixFilePermission>
    ) {
        when (isDirectory(source)) {
            true -> FileUtilsHelper.copyFolder(source.toPath(), target.toPath(), asFileAttribute(permissions))
            false -> FileUtilsHelper.copyFile(source.toPath(), target.toPath(), asFileAttribute(permissions))
        }
    }

    fun getOrCreateFolder(
        folder: Path,
        permissions: Set<PosixFilePermission>
    ): Path {
        require(exists(folder).not() || isDirectory(folder.toFile())) { "'$folder' points to a file" }
        createFolderIfNotExist(folder.parent, permissions)
        createFolderIfNotExist(folder, permissions)
        return folder
    }

    fun reCreateFolder(folder: File, permissions: Set<PosixFilePermission>): File {
        deleteFile(folder)
        getOrCreateFolder(folder.toPath(), permissions)
        return folder
    }

    fun createEmptyFolder(
        folder: Path,
        permissions: Set<PosixFilePermission>
    ): File {
        deleteFile(folder.toFile())
        Files.createDirectories(folder, asFileAttribute(permissions))
        return folder.toFile()
    }

    fun createParentFolders(
        folder: Path,
        permissions: Set<PosixFilePermission>
    ) {
        Files.createDirectories(folder.parent, asFileAttribute(permissions))
    }

    fun deleteFile(file: File) {
        when {
            isDirectory(file) -> FileUtilsHelper.deleteFolder(file.toPath())
            exists(file.toPath()) -> Files.delete(file.toPath())
        }
    }

    fun moveFile(
        source: File,
        target: File,
        permissions: Set<PosixFilePermission> = ONLY_USER
    ) {
        deleteFile(target)
        Files.move(source.toPath(), createParentDirectories(target.toPath(), asFileAttribute(permissions)))
    }

    fun createHardLink(
        source: File,
        target: File
    ) {
        val permissions = asFileAttribute(Files.getPosixFilePermissions(source.toPath()))
        when (isDirectory(source)) {
            true -> createFolderHardLinks(source.toPath(), target.toPath(), permissions)
            false -> createFileHardLink(source.toPath(), target.toPath(), permissions)
        }
    }

    fun writeContent(
        source: File,
        content: String,
        permissions: Set<PosixFilePermission> = ONLY_USER
    ) {
        Files.write(createParentDirectories(source.toPath(), asFileAttribute(permissions)), content.toByteArray())
    }

    fun isDirectory(file: File): Boolean = Files.isDirectory(file.toPath())

    fun size(file: File): Long = Files.size(file.toPath())

    fun listFiles(file: File): List<File> {
        return when (isDirectory(file)) {
            true -> Files.list(file.toPath()).map { it.toFile() }.toList()
            else -> emptyList()
        }
    }
}

@Suppress("TooManyFunctions")
internal object FileUtilsHelper {
    fun createFolderIfNotExist(file: Path, permissions: Set<PosixFilePermission>) {
        if (exists(file).not()) Files.createDirectories(file, asFileAttribute(permissions))
    }

    fun createFolderHardLinks(source: Path, target: Path, attributes: FileAttribute<*>) {
        deleteFolder(target)
        Files.walkFileTree(source, HardLinkFileVisitor(source, target, attributes))
    }

    fun createFileHardLink(source: Path, target: Path, attributes: FileAttribute<*>) {
        deleteFolder(target)
        Files.createLink(source, createParentDirectories(target, attributes))
    }

    fun copyFolder(source: Path, target: Path, attributes: FileAttribute<*>) {
        deleteFolder(target)
        Files.walkFileTree(source, CopyFileVisitor(source, target, attributes))
    }

    fun copyFile(source: Path, target: Path, attributes: FileAttribute<*>) {
        Files.copy(source, createParentDirectories(target, attributes), StandardCopyOption.REPLACE_EXISTING)
    }

    fun createParentDirectories(path: Path, attributes: FileAttribute<*>): Path {
        createDirectories(path.parent, attributes)
        return path
    }

    fun createDirectories(path: Path, attributes: FileAttribute<*>): Path {
        Files.createDirectories(path, attributes)
        return path
    }

    fun deleteFolder(path: Path) {
        if (exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }
    }
}
