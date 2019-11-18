package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File

class TempFileGenerator(private val properties: ApplicationProperties) {
    fun asFiles(files: Array<MultipartFile>): List<File> = files.map { asFile(it) }

    fun asFile(file: MultipartFile): File {
        val tempFile = File(properties.tempDirPath, FilenameUtils.getName(file.originalFilename))
        file.transferTo(tempFile)
        return tempFile
    }
}
