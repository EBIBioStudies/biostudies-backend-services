package ebi.ac.uk.io

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object NfsFileUtils {

    fun deleteFolder(file: File) {
        deleteFolder(file.toPath())
    }

    fun reCreateDirectory(file: File): File {
        val path = file.toPath()
        deleteFolder(path)
        Files.createDirectory(path)
        return file
    }

    fun replaceFile(source: File, target: File) {
        Files.copy(source.toPath(), createParentDirectories(target.toPath()), StandardCopyOption.REPLACE_EXISTING)
    }

    fun moveFile(source: File, target: File) {
        Files.move(source.toPath(), createParentDirectories(deleteIfExist(target.toPath())))
    }

    private fun createParentDirectories(path: Path): Path {
        Files.createDirectories(path.parent)
        return path
    }

    private fun deleteIfExist(path: Path): Path {
        deleteFolder(path)
        return path
    }

    private fun deleteFolder(path: Path) {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }
    }
}
