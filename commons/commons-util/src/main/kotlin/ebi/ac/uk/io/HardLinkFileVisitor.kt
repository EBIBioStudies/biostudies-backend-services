package ebi.ac.uk.io

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute

internal class HardLinkFileVisitor(
    private var sourcePath: Path,
    private val targetPath: Path,
    private val attributes: FileAttribute<*>
) : SimpleFileVisitor<Path>() {
    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        FileUtilsHelper.createDirectories(targetPath.resolve(sourcePath.relativize(dir)), attributes)
        return FileVisitResult.CONTINUE
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        Files.createLink(targetPath.resolve(sourcePath.relativize(file)), file)
        return FileVisitResult.CONTINUE
    }
}
