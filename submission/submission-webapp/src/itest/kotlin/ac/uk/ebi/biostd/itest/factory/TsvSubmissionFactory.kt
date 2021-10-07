package ac.uk.ebi.biostd.itest.factory

import ebi.ac.uk.dsl.tsv.Tsv
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import org.assertj.core.api.Assertions.assertThat

fun invalidLinkUrl() = tsv {
    line("Submission", "S-EPMC124")
    line("Title", "venous blood, Monocyte")
    line()

    line("Study", "SECT-001")
    line("Project", "CEEHRC (McGill)")
    line("<Organization>", "Org1")
    line("Tissue type", "venous blood")
    line("[Ontology]", "UBERON")
    line("(Tissue)", "Blood")
    line()

    line("Link")
}

fun assertAllInOneSubmissionTsv(tsv: String, accNo: String) {
    val lines = tsv.split("\n")

    val expectedSubmission = tsv {
        line("Submission", accNo)
        line("Title", "venous blood, ∆Monocyte")
        line("ReleaseDate", "2021-02-12")
        line()
    }
    assertTsvBlock(lines, 1, 4, expectedSubmission)

    val expectedRootSection = tsv {
        line("Study", "SECT-001")
        line("Project", "CEEHRC (McGill)")
        line("<Organization>", "Org1")
        line("Tissue type", "venous blood")
        line("(Tissue)", "Blood")
        line("[Ontology]", "UBERON")
        line("File List", "file-list.pagetab.tsv")
        line()
    }
    assertTsvBlock(lines, 5, 12, expectedRootSection)

    val expectedRootSectionLink = tsv {
        line("Link", "AF069309")
        line("type", "gen")
        line()
    }
    assertTsvBlock(lines, 13, 15, expectedRootSectionLink)

    val expectedRootSectionFile = tsv {
        line("File", "DataFile1.txt")
        line("Description", "Data File 1")
        line()
    }
    assertTsvBlock(lines, 16, 18, expectedRootSectionFile)

    val expectedRootSectionFilesTable = tsv {
        line("Files", "Description", "Type")
        line("DataFile2.txt", "Data File 2", "Data")
        line("Folder1/DataFile3.txt", "Data File 3", "Data")
        line("Folder1/Folder2/DataFile4.txt", "Data File 4", "Data")
        line()
    }
    assertTsvBlock(lines, 19, 23, expectedRootSectionFilesTable)

    val expectedSubsection = tsv {
        line("Stranded Total RNA-Seq", "SUBSECT-001", "SECT-001")
        line()
    }
    assertTsvBlock(lines, 24, 25, expectedSubsection)

    val expectedSubsectionLinksTable = tsv {
        line("Links", "Type", "Assay type", "(TermId)", "[Ontology]")
        line("EGAD00001001282", "EGA", "RNA-Seq", "EFO_0002768", "EFO")
        line()
    }
    assertTsvBlock(lines, 26, 28, expectedSubsectionLinksTable)

    val expectedSubsectionsTable = tsv {
        line("Data[SECT-001]", "Title", "Description")
        line("DT-1", "Group 1 Transcription Data", "The data for zygotic transcription in mammals group 1")
        line()
    }
    assertTsvBlock(lines, 29, 31, expectedSubsectionsTable)
}

private fun assertTsvBlock(lines: List<String>, from: Int, to: Int, expected: Tsv) {
    assertThat(lines.subList(from - 1, to).joinToString("\n")).isEqualTo(expected.toString())
}
