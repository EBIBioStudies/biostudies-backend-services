package ebi.ac.uk.io

import ebi.ac.uk.io.FileUtilsHelper.createDirectories
import ebi.ac.uk.io.FileUtilsHelper.createFileHardLink
import ebi.ac.uk.io.FileUtilsHelper.createFolderHardLinks
import ebi.ac.uk.io.FileUtilsHelper.createFolderIfNotExist
import ebi.ac.uk.io.FileUtilsHelper.createParentDirectories
import ebi.ac.uk.io.FileUtilsHelper.createSymLink
import ebi.ac.uk.io.ext.notExist
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Files.exists
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.streams.toList

val ONLY_USER: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwx------")
val READ_ONLY_GROUP: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-x---")
val ALL_CAN_READ: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-xr-x")
val GROUP_EXECUTE = PosixFilePermissions.fromString("rwx--x---")
val ALL_GROUP = PosixFilePermissions.fromString("rwxrwx---")

@Suppress("TooManyFunctions")
object FileUtils {
    fun copyOrReplaceFile(
        source: File,
        target: File,
        permissions: Set<PosixFilePermission>
    ) {
        when (isDirectory(source)) {
            true -> FileUtilsHelper.copyFolder(source.toPath(), target.toPath(), permissions)
            false -> FileUtilsHelper.copyFile(source.toPath(), target.toPath(), permissions)
        }

        Files.setPosixFilePermissions(target.toPath(), permissions)
    }

    fun copyOrReplaceFile(
        source: InputStream,
        target: File,
        permissions: Set<PosixFilePermission>
    ) {
        FileUtilsHelper.copyFile(source, target.toPath(), permissions)
        Files.setPosixFilePermissions(target.toPath(), permissions)
    }

    fun getOrCreateFolder(
        folder: Path,
        permissions: Set<PosixFilePermission>
    ): Path {
        require(exists(folder).not() || isDirectory(folder.toFile())) { "'$folder' points to a file" }
        createFolderIfNotExist(folder, permissions)
        return folder
    }

    fun reCreateFolder(file: File, permissions: Set<PosixFilePermission>): File {
        deleteFile(file)
        getOrCreateFolder(file.toPath(), permissions)
        return file
    }

    fun createEmptyFolder(
        folder: Path,
        permissions: Set<PosixFilePermission>
    ) {
        deleteFile(folder.toFile())
        createDirectories(folder, permissions)
    }

    fun createParentFolders(
        folder: Path,
        permissions: Set<PosixFilePermission>
    ) {
        createDirectories(folder.parent, permissions)
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

        Files.move(source.toPath(), createParentDirectories(target.toPath(), permissions))
        Files.setPosixFilePermissions(target.toPath(), permissions)
    }

    fun createHardLink(
        source: File,
        target: File
    ) {
        val permissions = Files.getPosixFilePermissions(source.toPath())
        when (isDirectory(source)) {
            true -> createFolderHardLinks(source.toPath(), target.toPath(), permissions)
            false -> createFileHardLink(source.toPath(), target.toPath(), permissions)
        }
    }

    fun createSymbolicLink(path: Path, symLinkPath: Path, permissions: Set<PosixFilePermission> = ONLY_USER) {
        createSymLink(path, symLinkPath, permissions)
    }

    fun writeContent(
        source: File,
        content: String,
        permissions: Set<PosixFilePermission> = ONLY_USER
    ) {
        val filePath = source.toPath()
        Files.write(createParentDirectories(source.toPath(), permissions), content.toByteArray())
        Files.setPosixFilePermissions(filePath, permissions)
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
        if (exists(file).not()) createDirectories(file, permissions)
    }

    fun createFolderHardLinks(source: Path, target: Path, permissions: Set<PosixFilePermission>) {
        deleteFolder(target)
        Files.walkFileTree(source, HardLinkFileVisitor(source, target, permissions))
    }

    fun createFileHardLink(source: Path, target: Path, permissions: Set<PosixFilePermission>) {
        deleteFolder(target)
        Files.createLink(source, createParentDirectories(target, permissions))
    }

    fun createSymLink(link: Path, target: Path, permissions: Set<PosixFilePermission>) {
        Files.createSymbolicLink(createParentDirectories(link, permissions), target)
    }

    fun copyFolder(source: Path, target: Path, permissions: Set<PosixFilePermission>) {
        deleteFolder(target)
        Files.walkFileTree(source, CopyFileVisitor(source, target, permissions))
    }

    fun copyFile(source: Path, target: Path, permissions: Set<PosixFilePermission>) {
        Files.copy(source, createParentDirectories(target, permissions), StandardCopyOption.REPLACE_EXISTING)
    }

    fun copyFile(source: InputStream, target: Path, permissions: Set<PosixFilePermission>) {
        Files.copy(source, createParentDirectories(target, permissions), StandardCopyOption.REPLACE_EXISTING)
    }

    fun createParentDirectories(path: Path, permissions: Set<PosixFilePermission>): Path {
        createDirectories(path.parent, permissions)
        return path
    }

    fun createDirectories(directoryPath: Path, permissions: Set<PosixFilePermission>): Path {
        var parent = directoryPath.root
        for (path in parent.relativize(directoryPath)) {
            parent = parent.resolve(path)
            if (parent.notExist()) createDirectory(parent, permissions)
        }
        return directoryPath
    }

    private fun createDirectory(path: Path, permissions: Set<PosixFilePermission>) {
        Files.createDirectory(path)
        Files.setPosixFilePermissions(path, permissions)
    }

    fun deleteFolder(path: Path) {
        if (exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }
    }
}
