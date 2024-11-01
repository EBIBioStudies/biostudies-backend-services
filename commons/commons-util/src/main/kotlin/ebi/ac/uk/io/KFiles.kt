package ebi.ac.uk.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
}
