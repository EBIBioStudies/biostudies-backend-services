package ac.uk.ebi.pmc.load

import ac.uk.ebi.biostd.common.SerializationConfig
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.title
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class PmcSubmissionTabProcessorTest {
    private val processor = PmcSubmissionTabProcessor(SerializationConfig.serializationService())

    @Test
    fun `overwrites release date with the first day of the publication year`() {
        val pagetab =
            tsv {
                line("Submission", "S-EPMC1234567")
                line("ReleaseDate", "2018-01-01")
                line()

                line("Study")
                line()

                line("Publication")
                line("Publication date", "2020-08-19")
                line()
            }.toString()

        val submission = processor.transformSubmission(pagetab)

        assertThat(submission.releaseDate).isEqualTo("2020-01-01")
        assertThat(submission.attributes.filter { it.name == "ReleaseDate" }).hasSize(1)
    }

    @Test
    fun `adds a missing release date when only the publication year is supplied`() {
        val pagetab =
            tsv {
                line("Submission", "S-EPMC1234567")
                line()

                line("Study")
                line()

                line("Publication")
                line("Publication date", "2019")
                line()
            }.toString()

        val submission = processor.transformSubmission(pagetab)

        assertThat(submission.releaseDate).isEqualTo("2019-01-01")
        assertThat(submission.attributes.filter { it.name == "ReleaseDate" }).hasSize(1)
    }

    @Test
    fun `preserves release date metadata and quoted values during normalization`() {
        val title = "A \"quoted\" title"
        val description = "Text\twith tabs\nand a blank line\n\nin text"
        val pagetab =
            tsv {
                line("Submission", "S-EPMC1234567")
                line("ReleaseDate", "2018-01-01")
                line("[source]", "PMC")
                line("Title", title)
                line("Description", "\"$description\"")
                line()

                line("Study")
                line()

                line("Publication")
                line("Publication date", "2020 May")
                line()
            }.toString()

        val submission = processor.transformSubmission(pagetab)

        assertThat(submission.releaseDate).isEqualTo("2020-01-01")
        assertThat(submission.title).isEqualTo(title)
        assertThat(submission.attributes.single { it.name == "Description" }.value).isEqualTo(description)
        assertThat(submission.attributes.first().valueAttrs.single().value).isEqualTo("PMC")
    }

    @Test
    fun `rejects submissions without a publication section during load`() {
        val pagetab =
            tsv {
                line("Submission", "S-EPMC1234567")
                line("ReleaseDate", "2018-01-01")
                line()

                line("Study")
                line()
            }.toString()

        assertThatThrownBy { processor.transformSubmission(pagetab) }
            .isInstanceOf(NoSuchElementException::class.java)
    }

    @Test
    fun `rejects publication dates without a year during load`() {
        val pagetab =
            tsv {
                line("Submission", "S-EPMC1234567")
                line("ReleaseDate", "2018-01-01")
                line()

                line("Study")
                line()

                line("Publication")
                line("Publication date", "unknown")
                line()
            }.toString()

        assertThatThrownBy { processor.transformSubmission(pagetab) }
            .isInstanceOf(NullPointerException::class.java)
    }
}
