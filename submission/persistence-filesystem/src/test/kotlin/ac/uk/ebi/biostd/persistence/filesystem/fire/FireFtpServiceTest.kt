package ac.uk.ebi.biostd.persistence.filesystem.fire

import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.FireFile
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FileSystemEntry
import uk.ac.ebi.fire.client.model.FireApiFile
import java.util.UUID

@ExtendWith(MockKExtension::class)
class FireFtpServiceTest(
    @MockK private val fireClient: FireClient,
) {
    private val testInstance = FireFtpService(fireClient)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `release submission file`() {
        val fireFile = FireFile(
            fireId = "fireId",
            firePath = null,
            published = false,
            filePath = "folder/myFile",
            relPath = "Files/folder/myFile",
            md5 = "md5",
            size = 12,
            type = ExtFileType.FILE,
            attributes = emptyList()
        )
        val apiFile = FireApiFile(
            objectId = 456,
            filesystemEntry = FileSystemEntry(path = "fire-path", published = true),
            fireOid = UUID.randomUUID().toString(),
            objectMd5 = "the-md5",
            objectSize = 123L,
            createTime = "2022-09-21"
        )
        coEvery { fireClient.publish(fireFile.fireId) } answers { apiFile }

        val file = runBlocking { testInstance.releaseSubmissionFile(fireFile, "/rel/path") }

        assertThat(file.published).isTrue()
        assertThat(file.firePath).isEqualTo("fire-path")
    }
}
