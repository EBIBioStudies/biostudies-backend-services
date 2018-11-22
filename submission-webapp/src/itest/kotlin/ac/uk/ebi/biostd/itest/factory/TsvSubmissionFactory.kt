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
    line("[Ontology]", "UBERON")
    line("(Tissue)", "Blood")
    line()
//
    line("Experiment[]", "Title", "Type")
    line("EXP-001", "Experiment 1", "Imaging")
    line()

//    line("File", "plates/J_Sero_plate_keys.xlsx")
//    line("Description", "Summary data")
//    line("Type", "XLSX File")
//    line()

//    line("Files", "Description", "Type")
//    line("plates/J_Sero_plate_keys.xlsx", "Summary data", "Library File")
//    line("plates/Plate01.csv", "Data for Plate 01", "Plate Details File")
//    line()

    line("Link", "AF069309")
    line("Type", "gen")
    line()

    line("Stranded Total RNA-Seq", "SUBSECT-001", "SECT-001")
    line()

    line("Data[SUBSECT-001]", "Title", "Description")
    line("DT-1", "Group 1 Transcription Data", "The data for zygotic transcription in mammals group 1")
    line()

    line("Links", "Type", "Assay type")
    line("EGAD00001001282", "EGA", "RNA-Seq")
    line()
}
