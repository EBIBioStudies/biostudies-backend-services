package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.submission.Submission
import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import org.junit.Test


class TsvParserTest {

    private val testInstance: TsvSerializer = TsvSerializer()

    @Test
    fun parseSimpleSubmission() {
        val sub = createVenousBloodMonocyte()
        val tsvString = TsvString(testInstance.serialize(sub))

        assertSubmission(tsvString, sub)
        assertSection(tsvString)
        assertSecondSection(tsvString)
    }

    private fun assertSubmission(tsvString: TsvString, sub: Submission) {
        assertThat(tsvString[0]).contains(accNoKey, "S-IHECRE00000919.1", "Public")
        assertThat(tsvString[1]).contains(titleKey, sub.title)
        assertThat(tsvString[2]).contains(releaseDateKey, "2015-02-20T06:30:00")
        assertThat(tsvString[3]).contains(rootPathKey, sub.rootPath)
        assertThat(tsvString[4]).contains("DataSource", "BLUEPRINT")
        assertThat(tsvString[5]).contains("AttachTo", "blueprint")
        assertThat(tsvString[6]).isEmptyLine()
    }

    private fun assertSection(tsvString: TsvString) {
        assertThat(tsvString[7]).contains("Study")
        assertThat(tsvString[8]).contains("Title", "venous blood, Monocyte")
        assertThat(tsvString[9]).contains("Project", "CEEHRC (McGill)")
        assertThat(tsvString[10]).contains("Status", "Incomplete")
        assertThat(tsvString[11]).contains("Organism", "Homo sapiens")
        assertThat(tsvString[12]).contains("Tissue type", "venous blood")
        assertThat(tsvString[13]).contains("[Ontology]", "UBERON")
        assertThat(tsvString[14]).contains("Donor ID", "McGill0139")
        assertThat(tsvString[15]).contains("Biomaterial Type", "primary cells")
        assertThat(tsvString[16]).contains("Cell Type", "Monocyte")
        assertThat(tsvString[17]).contains("[Ontology]", "CL")
        assertThat(tsvString[18]).contains("Disease", "Systemic Lupus Erythematosus")
        assertThat(tsvString[19]).contains("[Ontology]", "EFO")
        assertThat(tsvString[20]).contains("Experiment type", "Single donor")

        assertThat(tsvString[21]).isEmptyLine()

        assertThat(tsvString[22]).contains("Link", "IHECRE00000919.1")
        assertThat(tsvString[23]).contains("Type", "EpiRR")
    }

    private fun assertSecondSection(tsvString: TsvString) {
        assertThat(tsvString[24]).isEmptyLine()
        assertThat(tsvString[25]).contains("Stranded Total RNA-Seq")
        assertThat(tsvString[26]).isEmptyLine()
        assertThat(tsvString[27]).contains("Links", "Type", "Assay type", "Experiment type", "Primary id")
        assertThat(tsvString[28]).contains("EGAD00001001282", "EGA", "RNA-Seq", "Stranded Total RNA-Seq", "EGAX00001273202")
    }
}