package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import ebi.ac.uk.model.constans.SubFields
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SimpleSubmissionTsvParserTest {

    private val testInstance: TsvSerializer = TsvSerializer()

    @Test
    fun parseSimpleSubmission() {
        val sub = createVenousBloodMonocyte()
        val tsvString = testInstance.serialize(sub)

        val expected = tsv {
            line(ACC_NO_KEY, "S-IHECRE00000919.1", "Public")
            line(TITLE_KEY, sub.title)
            line(SubFields.RELEASE_TIME, "2015-02-20T06:30:00Z")
            line(ROOT_PATH_KEY, sub.rootPath)
            line("DataSource", "BLUEPRINT")
            line("AttachTo", "blueprint")
            line()

            line("Study")
            line("type", "Study")
            line("Title", "venous blood, Monocyte")
            line("Project", "CEEHRC (McGill)")
            line("Status", "Incomplete")
            line("Organism", "Homo sapiens")
            line("Tissue type", "venous blood")
            line("[Ontology]", "UBERON")
            line("Donor ID", "McGill0139")
            line("Biomaterial Type", "primary cells")
            line("Cell Type", "Monocyte")
            line("[Ontology]", "CL")
            line("Disease", "Systemic Lupus Erythematosus")
            line("[Ontology]", "EFO")
            line("Experiment type", "Single donor")
            line()

            line("Link", "IHECRE00000919.1")
            line("Type", "EpiRR")
            line()

            line("Stranded Total RNA-Seq")
            line("type", "Stranded Total RNA-Seq")
            line()

            line("Links", "Type", "Assay type", "Experiment type", "Primary id")
            line("EGAD00001001282", "EGA", "RNA-Seq", "Stranded Total RNA-Seq", "EGAX00001273202")
            line()
        }.toString()

        assertThat(tsvString).isEqualToIgnoringWhitespace(expected)
    }
}