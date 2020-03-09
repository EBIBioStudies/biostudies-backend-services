package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.model.SubmissionDb
import ac.uk.ebi.biostd.persistence.test.assertDbExtSection
import ac.uk.ebi.biostd.persistence.test.extSection
import org.junit.jupiter.api.Test

class ToDbSectionTest {
    @Test
    fun toDbSection() {
        val extSection = extSection
        val dbSubmission = SubmissionDb()
        val dbSection = extSection.toDbSection(dbSubmission, 0)

        assertDbExtSection(dbSection)
    }
}
