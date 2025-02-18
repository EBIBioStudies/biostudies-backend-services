package ebi.ac.uk.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object KFiles {
    suspend fun createTempFile(
        prefix: String,
        suffix: String,
    ): Path =
        withContext(Dispatchers.IO) {
            Files.createTempFile(prefix, suffix)
        }

    suspend fun createTempFile(filename: String): Path =
        withContext(Dispatchers.IO) {
            val tempDir = Files.createTempDirectory("donwloaded-files")
            val file = File(tempDir.toFile(), filename)
            file.createNewFile()
            return@withContext file.toPath()
        }
}
