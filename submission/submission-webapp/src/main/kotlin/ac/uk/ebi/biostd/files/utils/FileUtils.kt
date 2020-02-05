package ac.uk.ebi.biostd.files.utils

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

internal fun copyFile(basePath: Path, file: MultipartFile) {
    val expectedFilePath = basePath
        .resolve(file.originalFilename)
        .toFile()
    expectedFilePath.mkdirs()
    Files.copy(file.inputStream, expectedFilePath.toPath(), StandardCopyOption.REPLACE_EXISTING)
}

internal fun moveFile(basePath: Path, file: File) {
    val expectedFilePath = basePath
        .resolve(file.name)
        .toFile()
    expectedFilePath.mkdirs()
    Files.move(file.toPath(), expectedFilePath.toPath(), StandardCopyOption.REPLACE_EXISTING)
}
