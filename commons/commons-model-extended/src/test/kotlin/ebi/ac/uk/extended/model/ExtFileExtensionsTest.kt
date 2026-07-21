package ebi.ac.uk.extended.model

import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.FileSourceType.SUBMISSION
import ebi.ac.uk.extended.model.FileSourceType.USER
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(MockKExtension::class)
class ExtFileExtensionsTest {
    @Test
    fun `copy fire file`() {
        val fireFile =
            FireFile(
                fireId = "fire-id",
                firePath = "fire-path",
                published = true,
                filePath = "file-path",
                relPath = "rel-path",
                md5 = "md5",
                size = 1L,
                type = FILE,
                attributes = listOf(ExtAttribute("Attribute", "Old")),
                sourceType = SUBMISSION,
            )
        val newAttributes = listOf(ExtAttribute("Override", "New"))
        val copied = fireFile.typeSafeCopy(newAttributes, USER)

        assertThat(copied).usingRecursiveComparison().ignoringFields("attributes", "sourceType").isEqualTo(fireFile)
        assertThat(copied.attributes).isEqualTo(newAttributes)
        assertThat(copied.sourceType).isEqualTo(USER)
    }

    @Test
    fun `copy nfs file`(
        @MockK file: File,
    ) {
        val nfsFile =
            NfsFile(
                filePath = "file-path",
                relPath = "rel-path",
                file = file,
                fullPath = "full-path",
                md5 = "md5",
                size = 1L,
                attributes = listOf(ExtAttribute("Attribute", "Old")),
                type = FILE,
                sourceType = USER,
            )
        val newAttributes = listOf(ExtAttribute("Override", "New"))
        val copied = nfsFile.typeSafeCopy(newAttributes, SUBMISSION)

        assertThat(copied).usingRecursiveComparison().ignoringFields("attributes", "sourceType").isEqualTo(nfsFile)
        assertThat(copied.attributes).isEqualTo(newAttributes)
        assertThat(copied.sourceType).isEqualTo(SUBMISSION)
    }
}
