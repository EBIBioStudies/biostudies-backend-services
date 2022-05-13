package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.common.TsvPagetabExtension
import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import ac.uk.ebi.biostd.tsv.serialization.ACC_NO_KEY
import ac.uk.ebi.biostd.tsv.serialization.ROOT_PATH_KEY
import ac.uk.ebi.biostd.tsv.serialization.TITLE_KEY
import ac.uk.ebi.biostd.tsv.serialization.TsvSerializer
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SimpleSubmissionTsvParserTest {
    private val testInstance: TsvSerializer = TsvSerializer(TsvPagetabExtension())

    @Test
    fun parseSimpleSubmission() {
        val sub = createVenousBloodMonocyte()
        val tsvString = testInstance.serialize(sub)

        val expected = tsv {
            line(ACC_NO_KEY, "S-IHECRE00000919.1")
            line(SubFields.RELEASE_DATE, "2015-02-20")
            line(ROOT_PATH_KEY, sub.rootPath!!)
            line(TITLE_KEY, sub.title!!)
            line("DataSource", "BLUEPRINT")
            line("AttachTo", "blueprint")
            line()

            line("Study", "SECT-001")
            line("Title", "venous blood, Monocyte")
            line("Project", "CEEHRC (McGill)")
            line("Status", "Incomplete")
            line("Organism", "Homo sapiens")
            line("Tissue type", "venous blood")
            line("(Tissue)", "Blood")
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

            line("File", "TestData.csv")
            line("Type", "data")
            line()

            line("Stranded Total RNA-Seq", "SUB-SECT-001", "SECT-001")
            line()

            line("Links", "Type", "Assay type", "Experiment type", "Primary id")
            line("EGAD00001001282", "EGA", "RNA-Seq", "Stranded Total RNA-Seq", "EGAX00001273202")
            line()

            line("Files", "Type")
            line("Results.xls", "Results File")
            line()

            line("Data[SECT-001]")
            line("DT-1")
            line()
        }.toString()

        assertThat(tsvString).isEqualToIgnoringWhitespace(expected)
    }
}
