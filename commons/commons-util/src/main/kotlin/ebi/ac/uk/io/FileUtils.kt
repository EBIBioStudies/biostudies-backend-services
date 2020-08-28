package ebi.ac.uk.io

import ebi.ac.uk.io.FileUtilsHelper.createDirectories
import ebi.ac.uk.io.FileUtilsHelper.createFileHardLink
import ebi.ac.uk.io.FileUtilsHelper.createFolderHardLinks
import ebi.ac.uk.io.FileUtilsHelper.createFolderIfNotExist
import ebi.ac.uk.io.FileUtilsHelper.createParentDirectories
import ebi.ac.uk.io.FileUtilsHelper.createSymLink
import ebi.ac.uk.io.Permissions.ONLY_USER
import ebi.ac.uk.io.ext.notExist
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Files.exists
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.attribute.PosixFilePermission
import kotlin.streams.toList

@Suppress("TooManyFunctions")
object FileUtils {
    fun copyOrReplaceFile(
        source: File,
        target: File,
        permissions: Permissions
    ) {
        when (isDirectory(source)) {
            true -> FileUtilsHelper.copyFolder(source.toPath(), target.toPath(), permissions)
            false -> FileUtilsHelper.copyFile(source.toPath(), target.toPath(), permissions)
        }
    }

    fun copyOrReplaceFile(
        source: InputStream,
        target: File,
        permissions: Permissions
    ) {
        FileUtilsHelper.copyFile(source, target.toPath(), permissions)
    }

    fun getOrCreateFolder(
        folder: Path,
        permissions: Permissions
    ): Path {
        require(exists(folder).not() || isDirectory(folder.toFile())) { "'$folder' points to a file" }
        createFolderIfNotExist(folder, permissions)
        return folder
    }

    fun reCreateFolder(file: File, permissions: Permissions): File {
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
        permissions: Permissions = ONLY_USER
    ) {
        deleteFile(target)

        Files.move(source.toPath(), createParentDirectories(target.toPath(), permissions.toPosix()))
        Files.setPosixFilePermissions(target.toPath(), permissions.toPosixNoExecute())
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

    fun createSymbolicLink(
        path: Path,
        symLinkPath: Path,
        permissions: Set<PosixFilePermission> = ONLY_USER.toPosix()
    ) {
        createSymLink(path, symLinkPath, permissions)
    }

    fun writeContent(
        source: File,
        content: String,
        permissions: Permissions = ONLY_USER
    ) {
        val filePath = source.toPath()
        Files.write(createParentDirectories(source.toPath(), permissions.toPosix()), content.toByteArray())
        Files.setPosixFilePermissions(filePath, permissions.toPosixNoExecute())
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
    fun createFolderIfNotExist(file: Path, permissions: Permissions) {
        if (exists(file).not()) createDirectories(file, permissions.toPosix())
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

    fun copyFolder(source: Path, target: Path, permissions: Permissions) {
        deleteFolder(target)
        Files.walkFileTree(source, CopyFileVisitor(source, target, permissions))
    }

    fun copyFile(source: Path, target: Path, permissions: Permissions) {
        Files.copy(source, createParentDirectories(target, permissions.toPosix()), REPLACE_EXISTING)
        Files.setPosixFilePermissions(target, permissions.toPosixNoExecute())
    }

    fun copyFile(source: InputStream, target: Path, permissions: Permissions) {
        Files.copy(source, createParentDirectories(target, permissions.toPosix()), REPLACE_EXISTING)
        Files.setPosixFilePermissions(target, permissions.toPosixNoExecute())
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
