package ac.uk.ebi.biostd.itest.factory

import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv

fun simpleSubmissionTsv() = tsv {
    line("Submission", "S-ABC123")
    line("Title", "Simple Submission")
    line()
}

fun allInOneSubmissionTsv() = tsv {
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

    line("Link", "AF069309")
    line("Type", "gen")
    line()

    line("File", "DataFile1.txt")
    line("Description", "Data File 1")
    line()

    line("Files", "Description", "Type")
    line("DataFile2.txt", "Data File 2", "Data")
    line("Folder1/DataFile3.txt", "Data File 3", "Data")
    line("Folder1/Folder2/DataFile4.txt", "Data File 4", "Data")
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
