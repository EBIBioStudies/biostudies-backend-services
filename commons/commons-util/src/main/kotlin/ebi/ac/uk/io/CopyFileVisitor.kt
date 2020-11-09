package ebi.ac.uk.io

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFilePermission

internal class CopyFileVisitor(
    private var sourcePath: Path,
    private val targetPath: Path,
    private val filePermissions: Set<PosixFilePermission>,
    private val folderPermissions: Set<PosixFilePermission>
) : SimpleFileVisitor<Path>() {
    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        FileUtilsHelper.createDirectories(targetPath.resolve(sourcePath.relativize(dir)), folderPermissions)
        return FileVisitResult.CONTINUE
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        val target = targetPath.resolve(sourcePath.relativize(file))
        Files.copy(file, target)
        Files.setPosixFilePermissions(target, filePermissions)

        return FileVisitResult.CONTINUE
    }
}
