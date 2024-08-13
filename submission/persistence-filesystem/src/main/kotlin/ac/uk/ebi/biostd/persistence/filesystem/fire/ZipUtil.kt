package ac.uk.ebi.biostd.persistence.filesystem.fire

import ebi.ac.uk.exception.CorruptedFileException
import ebi.ac.uk.io.FileUtils.listAllFilesWithRelPaths
import ebi.ac.uk.io.ext.md5
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
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

        checkZipIntegrity(sourceDir, zipFile)
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

    internal fun checkZipIntegrity(
        sourceDir: File,
        zipFile: File,
    ) {
        ZipFile(zipFile).use { zip ->
            listAllFilesWithRelPaths(sourceDir).forEach {
                val (path, sourceFile) = it
                val entry = zip.getEntry(path)

                if (entry == null || sourceFile.md5() != zip.md5(entry)) throw CorruptedFileException(sourceFile)
            }
        }
    }

    private fun ZipFile.md5(entry: ZipEntry): String = getInputStream(entry).use { DigestUtils.md5Hex(it).uppercase() }
}
