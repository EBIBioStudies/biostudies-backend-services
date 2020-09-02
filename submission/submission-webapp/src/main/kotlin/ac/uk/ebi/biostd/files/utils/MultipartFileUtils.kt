package ac.uk.ebi.biostd.files.utils

import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXRWX___
import ebi.ac.uk.io.RW_RW____
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path

internal fun transferTo(basePath: Path, file: MultipartFile) {
    val expectedFilePath = basePath.resolve(file.originalFilename!!)
    FileUtils.createParentFolders(expectedFilePath, RWXRWX___)
    FileUtils.copyOrReplaceFile(
        source = file.inputStream,
        target = expectedFilePath.toFile(),
        filePermissions = RW_RW____,
        folderPermissions = RWXRWX___)
}
