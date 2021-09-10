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
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.security.MessageDigest
import kotlin.streams.toList

internal const val CHECKSUM_SIGNUM = 1
internal const val BUFFER_SIZE = 12288
internal const val MD5_ALGORITHM = "MD5"
internal const val HEXADECIMAL_BASE = 16
val RW_______: Set<PosixFilePermission> = PosixFilePermissions.fromString("rw-------")
val RWX______: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwx------")
val RWX__X___: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwx--x---")
val RW_RW____: Set<PosixFilePermission> = PosixFilePermissions.fromString("rw-rw----")
val RWXRWX___: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxrwx---")
val RWXR_X___: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-x---")
val RW_R_____: Set<PosixFilePermission> = PosixFilePermissions.fromString("rw-r-----")
val RW_R__R__: Set<PosixFilePermission> = PosixFilePermissions.fromString("rw-r--r--")
val RWXR_XR_X: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwxr-xr-x")

@Suppress("TooManyFunctions")
object FileUtils {
    fun copyOrReplaceFile(
        source: File,
        target: File,
        permissions: Permissions
    ) = when (isDirectory(source)) {
        true -> FileUtilsHelper.copyFolder(source.toPath(), target.toPath(), permissions)
        false -> FileUtilsHelper.copyFile(source.toPath(), target.toPath(), permissions)
    }

    fun copyOrReplaceFile(
        source: InputStream,
        target: File,
        permissions: Permissions
    ) = FileUtilsHelper.copyFile(source, target.toPath(), permissions)

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

    fun createEmptyFolder(folder: Path, permissions: Set<PosixFilePermission>) {
        deleteFile(folder.toFile())
        createDirectories(folder, permissions)
    }

    fun createParentFolders(folder: Path, permissions: Set<PosixFilePermission>) {
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
        permissions: Permissions
    ) {
        deleteFile(target)
        when (isDirectory(source)) {
            true -> FileUtilsHelper.moveFolder(source.toPath(), target.toPath(), permissions)
            false -> FileUtilsHelper.moveFile(source.toPath(), target.toPath(), permissions)
        }
    }

    fun createHardLink(
        source: File,
        target: File,
        permissions: Permissions
    ) {
        when (isDirectory(source)) {
            true -> createFolderHardLinks(source.toPath(), target.toPath(), permissions)
            false -> createFileHardLink(source.toPath(), target.toPath(), permissions)
        }
    }

    fun createSymbolicLink(path: Path, symLinkPath: Path, permissions: Set<PosixFilePermission>) {
        createSymLink(path, symLinkPath, permissions)
    }

    fun writeContent(
        source: File,
        content: String,
        permissions: Permissions = Permissions(RW_______, RWX______),
    ) {
        val filePath = source.toPath()
        Files.write(createParentDirectories(source.toPath(), permissions.folder), content.toByteArray())
        Files.setPosixFilePermissions(filePath, permissions.file)
    }

    fun isDirectory(file: File): Boolean = Files.isDirectory(file.toPath())

    fun size(file: File): Long = Files.size(file.toPath())

    fun md5(file: File): String = if (file.isFile) calculateMd5(file) else ""

    fun listFiles(file: File): List<File> =
        if (isDirectory(file)) Files.list(file.toPath()).map { it.toFile() }.toList() else emptyList()

    fun setFolderPermissions(path: Path, permissions: Set<PosixFilePermission>) {
        Files.setPosixFilePermissions(path, permissions)
    }

    private fun calculateMd5(file: File): String {
        val digest = MessageDigest.getInstance(MD5_ALGORITHM)
        file.inputStream().buffered(BUFFER_SIZE).use { it.iterator().forEach(digest::update) }

        return digest.digest().joinToString("") { "%02x".format(it) }.toUpperCase()
    }
}

@Suppress("TooManyFunctions")
internal object FileUtilsHelper {
    fun createFolderIfNotExist(file: Path, permissions: Set<PosixFilePermission>) {
        if (exists(file).not()) createDirectories(file, permissions)
    }

    fun createFolderHardLinks(
        source: Path,
        target: Path,
        permissions: Permissions
    ) {
        deleteFolder(target)
        Files.walkFileTree(source, HardLinkFileVisitor(source, target, permissions))
    }

    fun createFileHardLink(
        source: Path,
        target: Path,
        permissions: Permissions
    ) {
        deleteFolder(target)
        Files.createLink(source, createParentDirectories(target, permissions.folder))
        Files.setPosixFilePermissions(target, permissions.file)
    }

    fun createSymLink(link: Path, target: Path, permissions: Set<PosixFilePermission>) {
        if (exists(link)) Files.delete(link)
        Files.createSymbolicLink(createParentDirectories(link, permissions), target)
    }

    fun copyFolder(
        source: Path,
        target: Path,
        permissions: Permissions
    ) {
        deleteFolder(target)
        Files.walkFileTree(source, CopyFileVisitor(source, target, permissions))
    }

    fun moveFolder(
        source: Path,
        target: Path,
        permissions: Permissions
    ) {
        deleteFolder(target)
        Files.walkFileTree(source, MoveFileVisitor(source, target, permissions))
        deleteFolder(source)
    }

    fun copyFile(
        source: Path,
        target: Path,
        permissions: Permissions
    ) {
        Files.copy(source, createParentDirectories(target, permissions.folder), REPLACE_EXISTING)
        Files.setPosixFilePermissions(target, permissions.file)
    }

    fun copyFile(
        source: InputStream,
        target: Path,
        permissions: Permissions
    ) {
        Files.copy(source, createParentDirectories(target, permissions.folder), REPLACE_EXISTING)
        Files.setPosixFilePermissions(target, permissions.file)
    }

    fun moveFile(
        source: Path,
        target: Path,
        permissions: Permissions
    ) {
        Files.move(source, createParentDirectories(target, permissions.folder), REPLACE_EXISTING)
        Files.setPosixFilePermissions(target, permissions.file)
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

data class Permissions(val file: Set<PosixFilePermission>, val folder: Set<PosixFilePermission>)
