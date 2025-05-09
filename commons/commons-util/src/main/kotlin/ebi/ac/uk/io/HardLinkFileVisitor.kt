package ebi.ac.uk.io

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

internal class HardLinkFileVisitor(
    private var sourcePath: Path,
    private val targetPath: Path,
    private val permissions: Permissions,
) : SimpleFileVisitor<Path>() {
    override fun preVisitDirectory(
        dir: Path,
        attrs: BasicFileAttributes,
    ): FileVisitResult {
        FileUtilsHelper.createDirectories(targetPath.resolve(sourcePath.relativize(dir)), permissions.subFolder)
        return FileVisitResult.CONTINUE
    }

    override fun visitFile(
        file: Path,
        attrs: BasicFileAttributes,
    ): FileVisitResult {
        val target = targetPath.resolve(sourcePath.relativize(file))
        FileUtilsHelper.createFileHardLink(filePath = file, target = target, permissions = permissions)
        return FileVisitResult.CONTINUE
    }
}
