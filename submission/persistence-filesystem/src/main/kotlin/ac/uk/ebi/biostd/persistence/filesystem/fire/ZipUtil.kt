package ac.uk.ebi.biostd.persistence.filesystem.fire

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.isDirectory

object ZipUtil {
    fun pack(
        sourceDir: File,
        zipFile: File,
    ) {
        ZipOutputStream(zipFile.outputStream()).use { zs ->
            val sourcePath = sourceDir.toPath()
            Files.walk(sourcePath)
                .filter { path -> path.isDirectory().not() }
                .forEach { path ->
                    zs.putNextEntry(createZipEntry(path, sourcePath))
                    Files.copy(path, zs)
                    zs.closeEntry()
                }
        }
    }

    fun unpack(
        zip: File,
        outputDir: File,
    ) {
        org.zeroturnaround.zip.ZipUtil.unpack(zip, outputDir)
    }

    private fun createZipEntry(
        filePath: Path,
        sourcePath: Path,
    ): ZipEntry {
        val zipEntry = ZipEntry(sourcePath.relativize(filePath).toString())
        zipEntry.time = 0
        return zipEntry
    }
}
