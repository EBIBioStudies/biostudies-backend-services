package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.extTestAttribute
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.sectionAttribute
import ac.uk.ebi.biostd.persistence.model.FileList
import ac.uk.ebi.biostd.persistence.model.Section
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.FilesSource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Collections.emptySortedSet

@ExtendWith(MockKExtension::class)
class ToExtSectionTest(
    @MockK val fileSource: FilesSource,
    @MockK val fileList: FileList,
    @MockK val extFileList: ExtFileList,
    @MockK val
) {
    private val section = Section(type = "type", accNo = "accNo")
        .also {
            it.fileList = fileList
            it.attributes = sortedSetOf(sectionAttribute)
            it.sections = emptySortedSet()
            it.files = emptySortedSet()
            it.links = emptySortedSet()
        }

    @Test
    fun toExtSection() {
        mockkStatic(TO_EXT_FILE_LIST_EXTENSION, TO_EXT_ATTRIBUTE_EXTENSIONS) {
            every { fileList.toExtFileList(fileSource) } returns extFileList
            every { sectionAttribute.toExtAttribute() } returns extTestAttribute

            val sectionResult = section.toExtSection(fileSource)
            assertThat(sectionResult.accNo).isEqualTo(section.accNo)
            assertThat(sectionResult.type).isEqualTo(section.type)
            assertThat(sectionResult.fileList).isEqualTo(extFileList)
            assertThat(sectionResult.attributes).containsExactly(extTestAttribute)
        }
    }
}
