package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File

class TempFileGenerator(private val properties: ApplicationProperties) {
    fun asFiles(files: Array<out MultipartFile>): List<File> = files.map { asFile(it) }

    fun asFile(file: MultipartFile): File {
        val tempFile = File(properties.tempDirPath, FilenameUtils.getName(file.originalFilename))
        file.transferTo(tempFile)
        return tempFile
    }
}
