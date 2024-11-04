package uk.ac.ebi.serialization.common

import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
internal class FilesResolverTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = FilesResolver(tempFolder.createDirectory("test-files"))

    @Test
    fun createRequestTempFile() {
        val file = testInstance.createRequestTempFile("accNo", 15, "file.json")
        val anotherFile = testInstance.createRequestTempFile("accNo", 15, "file.json")

        assertThat(file).exists()
        assertThat(anotherFile).exists()
        assertThat(file).isNotEqualTo(anotherFile)
    }

    @Test
    fun createTempFile() {
        val file = testInstance.createTempFile("file.json")
        val anotherFile = testInstance.createTempFile("file.json")

        assertThat(file).exists()
        assertThat(anotherFile).exists()
        assertThat(file).isNotEqualTo(anotherFile)
    }
}
