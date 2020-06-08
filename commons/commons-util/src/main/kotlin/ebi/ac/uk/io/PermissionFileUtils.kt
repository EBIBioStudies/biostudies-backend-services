package ebi.ac.uk.io

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions.asFileAttribute

object PermissionFileUtils {

    fun copyOrReplaceFile(source: File, target: File, permissions: Set<PosixFilePermission>) {
        when (FileUtils.isDirectory(source)) {
            true -> copyFolder(source.toPath(), target.toPath(), asFileAttribute(permissions))
            false -> copyFile(source.toPath(), target.toPath(), asFileAttribute(permissions))
        }
    }

    fun createFolder(folder: Path, permissions: Set<PosixFilePermission>) {
        FileUtils.deleteIfExist(folder)
        Files.createDirectories(folder.parent, asFileAttribute(permissions))
        Files.createDirectory(folder)
    }

    fun createParentFolders(folder: Path, permissions: Set<PosixFilePermission>) {
        Files.createDirectories(folder.parent, asFileAttribute(permissions))
    }

    private fun copyFolder(source: Path, target: Path, directoryAttributes: FileAttribute<*>) {
        FileUtils.deleteIfExist(target)
        Files.walkFileTree(source, AttributesCopyFileVisitor(source, target, directoryAttributes))
    }

    private fun copyFile(source: Path, target: Path, attributes: FileAttribute<*>) {
        Files.copy(source, createParentDirectories(target, attributes), StandardCopyOption.REPLACE_EXISTING)
    }

    private fun createParentDirectories(path: Path, attributes: FileAttribute<*>): Path {
        Files.createDirectories(path.parent, attributes)
        return path
    }
}
