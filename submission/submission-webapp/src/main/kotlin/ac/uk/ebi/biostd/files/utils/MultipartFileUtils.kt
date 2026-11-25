package ac.uk.ebi.biostd.files.utils

import org.springframework.web.multipart.MultipartFile
import uk.ac.ebi.biostd.io.FileUtils
import uk.ac.ebi.biostd.io.Permissions
import uk.ac.ebi.biostd.io.RWXRWX___
import uk.ac.ebi.biostd.io.RW_RW____
import java.nio.file.Path

internal fun transferTo(
    basePath: Path,
    file: MultipartFile,
) {
    val expectedFilePath = basePath.resolve(file.originalFilename!!)
    FileUtils.createParentFolders(expectedFilePath, RWXRWX___)
    FileUtils.copyOrReplaceFile(
        source = file,
        target = expectedFilePath.toFile(),
        permissions = Permissions(RW_RW____, RWXRWX___),
    )
}
