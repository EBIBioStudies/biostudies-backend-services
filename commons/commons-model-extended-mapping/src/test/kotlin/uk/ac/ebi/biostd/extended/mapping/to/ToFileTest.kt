package uk.ac.ebi.biostd.extended.mapping.to

import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtFileType.FILE
import uk.ac.ebi.biostd.extended.model.FireFile
import uk.ac.ebi.biostd.extended.model.NfsFile
import uk.ac.ebi.biostd.io.ext.md5
import uk.ac.ebi.biostd.io.ext.size
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.test.createFile

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class ToFileTest(
    @MockK val extAttribute: ExtAttribute,
    @MockK val attribute: Attribute,
    tempFolder: TemporaryFolder,
) {
    private var file = tempFolder.createFile("myFile", "test content")
    private val md5 = file.md5()
    private val extFile =
        NfsFile(
            "folder/myFile",
            "Files/folder/myFile",
            file,
            file.absolutePath,
            md5,
            file.size(),
            attributes = listOf(extAttribute),
        )

    @BeforeEach
    fun beforeEach() {
        mockkStatic(TO_ATTRIBUTE_EXTENSIONS)
        every { extAttribute.name } returns "attrName"
        every { extAttribute.toAttribute() } returns attribute
    }

    @Test
    fun `from nfs file`() {
        assertFile(extFile.toFile())
    }

    @Test
    fun `from fire file`() {
        val file =
            FireFile(
                fireId = "fireId",
                firePath = "firePath",
                published = false,
                filePath = "folder/myFile",
                relPath = "Files/folder/myFile",
                md5 = md5,
                size = 12,
                type = FILE,
                attributes = listOf(extAttribute),
            )
        assertFile(file.toFile())
    }

    private fun assertFile(file: BioFile) {
        assertThat(file.attributes).containsExactly(attribute)
        assertThat(file.size).isEqualTo(12L)
        assertThat(file.path).isEqualTo(extFile.filePath)
    }
}
