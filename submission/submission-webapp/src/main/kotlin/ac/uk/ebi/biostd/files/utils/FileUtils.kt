package ac.uk.ebi.biostd.files.utils

import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path

internal fun transferTo(basePath: Path, file: MultipartFile) {
    val expectedFilePath = basePath
        .resolve(file.originalFilename)
        .toFile()
    expectedFilePath.parentFile.mkdirs()
    file.transferTo(expectedFilePath)
}
