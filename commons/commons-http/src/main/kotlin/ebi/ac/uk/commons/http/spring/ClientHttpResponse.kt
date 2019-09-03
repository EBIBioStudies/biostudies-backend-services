package ebi.ac.uk.commons.http.spring

import org.springframework.http.client.ClientHttpResponse
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun ClientHttpResponse.saveInTempFile(filePrefix: String): File {
    val targetPath = Files.createTempFile(filePrefix, ".tmp")
    Files.copy(body, targetPath, StandardCopyOption.REPLACE_EXISTING)
    return targetPath.toFile()
}
