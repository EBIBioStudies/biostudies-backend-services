package ac.uk.ebi.biostd.submission.helpers

import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File

class TempFileGenerator(private val tempDirPath: String) {
    fun asFiles(files: List<MultipartFile>): List<File> = files.map { asFile(it) }

    fun asFile(file: MultipartFile): File {
        val tempFile = File(tempDirPath, FilenameUtils.getName(file.originalFilename))
        file.transferTo(tempFile)
        return tempFile
    }
}
