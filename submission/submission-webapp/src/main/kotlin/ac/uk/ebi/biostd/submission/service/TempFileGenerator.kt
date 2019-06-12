package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import org.springframework.web.multipart.MultipartFile
import java.io.File

class TempFileGenerator(private val properties: ApplicationProperties) {

    fun asFiles(files: Array<MultipartFile>): List<File> = files.map { createTmpFile(it) }

    private fun createTmpFile(file: MultipartFile): File {
        val tempFile = File(properties.tempDirPath, file.originalFilename)
        file.transferTo(tempFile)
        return tempFile
    }
}
