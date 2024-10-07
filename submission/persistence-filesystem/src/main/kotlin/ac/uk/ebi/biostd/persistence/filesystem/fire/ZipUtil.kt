package ac.uk.ebi.biostd.persistence.filesystem.fire

import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.isDirectory

private val logger = KotlinLogging.logger {}

object ZipUtil {
    fun pack(
        sourceDir: File,
        zipFile: File,
    ) {
        val sourcePath = sourceDir.toPath()
        val files =
            Files
                .walk(sourcePath)
                .filter { path -> path.isDirectory().not() }
                .toList()

        logger.info { "Starting zip folder creation for ${sourceDir.name}. totalFiles=${files.size}" }
        ZipOutputStream(zipFile.outputStream()).use { zs ->
            files
                .forEachIndexed { idx, path ->
                    val index = idx + 1
                    logger.info { "Starting file '$path', size='${Files.size(path)}' $index of ${files.size}" }

                    zs.putNextEntry(createZipEntry(path, sourcePath))
                    Files.copy(path, zs)
                    zs.closeEntry()

                    logger.info { "Completed file '$path', $index of ${files.size}" }
                }
        }

        logger.info { "Finished zip folder creation for ${sourceDir.name}" }
    }

    fun unpack(
        zip: File,
        outputDir: File,
    ) {
        org.zeroturnaround.zip.ZipUtil
            .unpack(zip, outputDir)
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
