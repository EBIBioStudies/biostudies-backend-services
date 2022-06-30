package ebi.ac.uk.io

import ebi.ac.uk.io.FileUtilsHelper.setPermissions
import mu.KotlinLogging
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

private val logger = KotlinLogging.logger {}

internal class HardLinkFileVisitor(
    private var sourcePath: Path,
    private val targetPath: Path,
    private val permissions: Permissions
) : SimpleFileVisitor<Path>() {
    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        FileUtilsHelper.createDirectories(targetPath.resolve(sourcePath.relativize(dir)), permissions.folder)
        return FileVisitResult.CONTINUE
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        val target = targetPath.resolve(sourcePath.relativize(file))

        logger.info { "Processing FTP link for file $file into target $target" }

        Files.createLink(target, file)
        setPermissions(target, permissions.file)

        logger.info { "Finished processing FTP link for file $file into target $target" }

        return FileVisitResult.CONTINUE
    }
}
