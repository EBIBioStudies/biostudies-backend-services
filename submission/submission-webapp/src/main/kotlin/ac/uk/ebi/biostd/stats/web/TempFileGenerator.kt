package ac.uk.ebi.biostd.stats.web

import ebi.ac.uk.io.FileUtils
import mu.KotlinLogging
import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Paths
import java.time.Clock
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

class TempFileGenerator(
    private val tempDirPath: String,
    private val clock: Clock,
) {
    fun asFiles(files: List<MultipartFile>): List<File> = files.map { asFile(it) }

    fun asFile(file: MultipartFile): File {
        val tempFile = baseFolder().resolve(FilenameUtils.getName(file.originalFilename))
        file.transferTo(tempFile)
        return tempFile
    }

    /**
     * Delete temporally folder folder from previous months to the current month. note that day is ignored so calling
     * method in any day of July (07) wtih 3 months will delete files from april and before.
     */
    fun deleteOldFiles(monthsOld: Long) {
        fun listSubDirectories() =
            File(tempDirPath)
                .walkTopDown()
                .maxDepth(1)
                .drop(1)
                .filter { it.isDirectory }

        val now = ZonedDateTime.now(clock).minusMonths(monthsOld)
        val deleteThreahold = "${now.year}${now.monthValue}".toInt()

        listSubDirectories()
            .forEach { folder ->
                val name = Paths.get(tempDirPath).relativize(folder.toPath())
                if (name.toString().toInt() < deleteThreahold) {
                    logger.info { "Deleting files in ${folder.absolutePath}" }
                    FileUtils.deleteFile(folder)
                }
            }
    }

    private fun baseFolder(): File {
        val now = ZonedDateTime.now(clock)
        val basePath =
            Paths
                .get(tempDirPath)
                .resolve("${now.year}${now.monthValue}")
                .resolve("${now.dayOfMonth}")
                .toFile()
        basePath.mkdirs()
        return basePath
    }
}
