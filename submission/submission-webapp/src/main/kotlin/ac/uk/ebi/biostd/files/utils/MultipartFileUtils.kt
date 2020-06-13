package ac.uk.ebi.biostd.files.utils

import ac.uk.ebi.biostd.files.service.UserFilesService
import ebi.ac.uk.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path

internal fun transferTo(basePath: Path, file: MultipartFile) {
    val expectedFilePath = basePath.resolve(file.originalFilename)
    FileUtils.createParentFolders(expectedFilePath, UserFilesService.FILE_PERMISSION)
    file.transferTo(expectedFilePath)
}
