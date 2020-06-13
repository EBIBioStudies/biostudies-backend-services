package ebi.ac.uk.io

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute

@Suppress("SpreadOperator")
internal class CopyFileVisitor(
    private var sourcePath: Path,
    private val targetPath: Path,
    private vararg val directoryAttributes: FileAttribute<*>
) : SimpleFileVisitor<Path>() {
    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)), *directoryAttributes)
        return FileVisitResult.CONTINUE
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        Files.copy(file, targetPath.resolve(sourcePath.relativize(file)))
        return FileVisitResult.CONTINUE
    }
}
