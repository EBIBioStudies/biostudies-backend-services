package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.Section
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.test.AttributeFactory.defaultAttribute
import uk.ac.ebi.extended.test.SectionFactory.defaultSection

@ExtendWith(MockKExtension::class)
class ToExtSectionTest(

) {

    private val fileList: FileList = mockk()
    private val section = Section(
        type = "Study",
        accNo = "accNo",
        fileList = fileList,
        attributes = listOf(Attribute("File List", "value"), Attribute("attribute1", "value1")),

    )
    private val filesSource: FilesSource = mockk()
    private val extFileList: ExtFileList = mockk()

    private val fileListMapper: FileListMapper = mockk()
    private val testInstance = SectionMapper(fileListMapper)

    private val expected = defaultSection("accNo", "Study", extFileList, attributes = listOf(defaultAttribute("attribute1", "value1")) )

    @Test
    fun toExtSection() {
        mockkStatic(TO_EXT_FILE_EXTENSIONS, TO_EXT_LINK_EXTENSIONS, TO_EXT_TABLE_EXTENSIONS)
        every { fileListMapper.toExtFileList(fileList, filesSource) } returns extFileList

        val result = testInstance.toExtSection(section, filesSource)

        assertThat(result).isEqualTo(expected)
    }
}
