package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.extTestAttribute2
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.extTestFile2
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.extTestLink2
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.extTestSection2
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.sectionAttribute
import ac.uk.ebi.biostd.persistence.model.File
import ac.uk.ebi.biostd.persistence.model.Link
import ac.uk.ebi.biostd.persistence.model.ReferencedFileList
import ac.uk.ebi.biostd.persistence.model.Section
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.sources.FilesSource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Collections.emptySortedSet
import java.util.SortedSet

@ExtendWith(MockKExtension::class)
class ToExtSectionTest(
    @MockK val fileSource: FilesSource,
    @MockK val fileList: ReferencedFileList,
    @MockK val extFileList: ExtFileList
) {
    private val sections: SortedSet<Section> = emptySortedSet()
    private val files: SortedSet<File> = emptySortedSet()
    private val links: SortedSet<Link> = emptySortedSet()

    private val section = Section(type = "type", accNo = "accNo")
        .also {
            it.fileList = fileList
            it.attributes = sortedSetOf(sectionAttribute)
            it.sections = sections
            it.files = files
            it.links = links
        }

    @Test
    fun toExtSection() {
        mockkStatic(
            TO_EXT_ATTRIBUTE_EXTENSIONS,
            TO_EXT_FILE_LIST_EXTENSION,
            TO_EXT_LINK_EXTENSIONS,
            TO_EXT_EITHER_LIST_EXTENSIONS) {
            every { fileList.toExtFileList(fileSource) } returns extFileList
            every { sectionAttribute.toExtAttribute() } returns extTestAttribute2
            every { sections.toExtSections(fileSource) } returns listOf(Either.left(extTestSection2))
            every { files.toExtFiles(fileSource) } returns listOf(Either.left(extTestFile2))
            every { links.toExtLinks() } returns listOf(Either.left(extTestLink2))

            val sectionResult = section.toExtSection(fileSource)

            assertThat(sectionResult.accNo).isEqualTo(section.accNo)
            assertThat(sectionResult.type).isEqualTo(section.type)
            assertThat(sectionResult.fileList).isEqualTo(extFileList)
            assertThat(sectionResult.attributes).containsExactly(extTestAttribute2)
            assertThat(sectionResult.sections).containsExactly(Either.left(extTestSection2))
            assertThat(sectionResult.files).containsExactly(Either.left(extTestFile2))
            assertThat(sectionResult.links).containsExactly(Either.left(extTestLink2))
        }
    }
}
