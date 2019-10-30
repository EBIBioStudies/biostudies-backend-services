package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.FILE_LIST_NAME
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.FILE_NAME
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.FILE_REF_NAME
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.assertExtAttribute
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.assertExtFile
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.assertExtLink
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.fileDb
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.linkDb
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.refFileListDb
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.sectAttributeDb
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.sectionDb
import ac.uk.ebi.biostd.persistence.model.Section
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.io.sources.FilesSource
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(value = [MockKExtension::class, TemporaryFolderExtension::class])
class ToExtSectionTest(@MockK val filesSource: FilesSource, tempFolder: TemporaryFolder) {
    private val systemFile1 = tempFolder.createFile(FILE_NAME)
    private val systemFile2 = tempFolder.createFile(FILE_REF_NAME)
    private val systemFile3 = tempFolder.createFile(FILE_LIST_NAME)

    private val section = Section(type = "type", accNo = "accNo")
        .also {
            it.fileList = refFileListDb
            it.attributes = sortedSetOf(sectAttributeDb)
            it.sections = sortedSetOf(sectionDb)
            it.files = sortedSetOf(fileDb)
            it.links = sortedSetOf(linkDb)
        }

    @Test
    fun toExtSection() {
        every { filesSource.getFile(FILE_NAME) } returns systemFile1
        every { filesSource.getFile(FILE_REF_NAME) } returns systemFile2
        every { filesSource.getFile(FILE_LIST_NAME) } returns systemFile3

        val extSection = section.toExtSection(filesSource)

        assertThat(extSection.accNo).isEqualTo(section.accNo)
        assertThat(extSection.attributes).hasSize(1)
        assertExtAttribute(extSection.attributes.first())

        assertThat(extSection.files).hasSize(1)
        assertThat(extSection.files.first()).hasLeftValueSatisfying { assertExtFile(it, systemFile1, FILE_NAME) }

        assertThat(extSection.links).hasSize(1)
        assertThat(extSection.links.first()).hasLeftValueSatisfying { assertExtLink(it) }

        assertThat(extSection.fileList).isNotNull()
        assertThat(extSection.fileList!!.file).isEqualTo(systemFile3)
        assertThat(extSection.fileList!!.fileName).isEqualTo(FILE_LIST_NAME)
    }
}
