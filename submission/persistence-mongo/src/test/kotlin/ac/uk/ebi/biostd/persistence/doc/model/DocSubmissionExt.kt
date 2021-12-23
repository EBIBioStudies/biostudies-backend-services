package ac.uk.ebi.biostd.persistence.doc.model

import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSection
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.subSection
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.subSectionTable
import ebi.ac.uk.extended.model.allSections
import ebi.ac.uk.test.basicExtSubmission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class DocSubmissionExt {
    @Test
    fun `get all sections from submission`() {
        val sections = basicExtSubmission.copy(section = rootSection).allSections
        assertThat(sections).hasSize(3)
        assertThat(sections).containsExactlyInAnyOrder(rootSection, subSection, subSectionTable)
    }

    @Test
    fun `get all sections from section`() {
        val sections = rootSection.allSections
        assertThat(sections).hasSize(2)
        assertThat(sections).containsExactlyInAnyOrder(subSection, subSectionTable)
    }
}
