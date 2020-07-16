package ac.uk.ebi.biostd.files.utils

import ebi.ac.uk.io.ALL_GROUP
import ebi.ac.uk.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path

internal fun transferTo(basePath: Path, file: MultipartFile) {
    val expectedFilePath = basePath.resolve(file.originalFilename)
    FileUtils.createParentFolders(expectedFilePath, ALL_GROUP)
    FileUtils.copyOrReplaceFile(file.inputStream, expectedFilePath.toFile(), ALL_GROUP)
}
