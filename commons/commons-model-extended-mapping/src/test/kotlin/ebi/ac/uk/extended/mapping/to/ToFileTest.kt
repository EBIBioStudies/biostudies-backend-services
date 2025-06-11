package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.test.createFile
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
            true,
            md5,
            file.size(),
            listOf(extAttribute),
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
