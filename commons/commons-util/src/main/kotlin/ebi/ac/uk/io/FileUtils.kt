package ebi.ac.uk.io

import ebi.ac.uk.io.FileUtilsHelper.createDirectories
import ebi.ac.uk.io.FileUtilsHelper.createFileHardLink
import ebi.ac.uk.io.FileUtilsHelper.createFolderHardLinks
import ebi.ac.uk.io.FileUtilsHelper.createFolderIfNotExist
import ebi.ac.uk.io.FileUtilsHelper.createParentDirectories
import ebi.ac.uk.io.FileUtilsHelper.createSymLink
import ebi.ac.uk.io.ext.notExist
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Files.exists
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.streams.toList

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
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) = when (isDirectory(source)) {
        true -> FileUtilsHelper.copyFolder(source.toPath(), target.toPath(), filePermissions, folderPermissions)
        false -> FileUtilsHelper.copyFile(source.toPath(), target.toPath(), filePermissions, folderPermissions)
    }

    fun copyOrReplaceFile(
        source: InputStream,
        target: File,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) = FileUtilsHelper.copyFile(source, target.toPath(), filePermissions, folderPermissions)

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
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        deleteFile(target)

        when (isDirectory(source)) {
            true -> FileUtilsHelper.moveFolder(source.toPath(), target.toPath(), filePermissions, folderPermissions)
            false -> FileUtilsHelper.moveFile(source.toPath(), target.toPath(), filePermissions, folderPermissions)
        }
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

    fun createSymbolicLink(path: Path, symLinkPath: Path, permissions: Set<PosixFilePermission> = RWX______) {
        createSymLink(path, symLinkPath, permissions)
    }

    fun writeContent(
        source: File,
        content: String,
        filePermissions: Set<PosixFilePermission> = RW_______,
        folderPermissions: Set<PosixFilePermission> = RWX______
    ) {
        val filePath = source.toPath()
        Files.write(createParentDirectories(source.toPath(), folderPermissions), content.toByteArray())
        Files.setPosixFilePermissions(filePath, filePermissions)
    }

    fun isDirectory(file: File): Boolean = Files.isDirectory(file.toPath())

    fun size(file: File): Long = Files.size(file.toPath())

    fun md5(file: File): String = if (file.isFile) DigestUtils.md5Hex(file.readBytes()).toUpperCase() else ""

    fun listFiles(file: File): List<File> = when (isDirectory(file)) {
        true -> Files.list(file.toPath()).map { it.toFile() }.toList()
        else -> emptyList()
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

    fun copyFolder(
        source: Path,
        target: Path,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        deleteFolder(target)
        Files.walkFileTree(source, CopyFileVisitor(source, target, filePermissions, folderPermissions))
    }

    fun moveFolder(
        source: Path,
        target: Path,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        deleteFolder(target)
        Files.walkFileTree(source, MoveFileVisitor(source, target, filePermissions, folderPermissions))
        deleteFolder(source)
    }

    fun copyFile(
        source: Path,
        target: Path,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        Files.copy(source, createParentDirectories(target, folderPermissions), REPLACE_EXISTING)
        Files.setPosixFilePermissions(target, filePermissions)
    }

    fun copyFile(
        source: InputStream,
        target: Path,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        Files.copy(source, createParentDirectories(target, folderPermissions), REPLACE_EXISTING)
        Files.setPosixFilePermissions(target, filePermissions)
    }

    fun moveFile(
        source: Path,
        target: Path,
        filePermissions: Set<PosixFilePermission>,
        folderPermissions: Set<PosixFilePermission>
    ) {
        Files.move(source, createParentDirectories(target, folderPermissions), REPLACE_EXISTING)
        Files.setPosixFilePermissions(target, filePermissions)
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
