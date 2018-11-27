package ac.uk.ebi.biostd.itest.factory

import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv

fun allInOneSubmissionTsv() = tsv {
    line("Submission", "S-EPMC124")
    line("Title", "venous blood, Monocyte")
    line()

    line("Study", "SECT-001")
    line("Project", "CEEHRC (McGill)")
    line("Tissue type", "venous blood")
    line()

    line("Link", "AF069309")
    line("Type", "gen")
    line()

    line("Stranded Total RNA-Seq", "SUBSECT-001", "SECT-001")
    line()

    line("Data[SECT-001]", "Title", "Description")
    line("DT-1", "Group 1 Transcription Data", "The data for zygotic transcription in mammals group 1")
    line()

    line("Links", "Type", "Assay type")
    line("EGAD00001001282", "EGA", "RNA-Seq")
    line()
}
