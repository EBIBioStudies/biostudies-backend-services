package ac.uk.ebi.biostd.itest.factory

import ac.uk.ebi.biostd.tsv.line
import ac.uk.ebi.biostd.tsv.tsv

fun allInOneSubmissionTsv() = tsv {
    line("Submission", "S-EPMC124")
    line("Title", "venous blood, Monocyte")
    line("ReleaseDate", "2016-06-07T10:46:46.000")
    line("RootPath", "S-EPMC124")
    line("DataSource", "BLUEPRINT")
    line()

    line("Study", "SECT-001")
    line("Title", "venous blood, Monocyte")
    line("Project", "CEEHRC (McGill)")
    line("Status", "Incomplete")
    line("Organism", "Homo sapiens")
    line("Tissue type", "venous blood")
    line("[Ontology]", "UBERON")
    line("(Tissue)", "Blood")
    line("Donor ID", "McGill0139")
    line("Biomaterial Type", "primary cells")
    line("Cell Type", "Monocyte")
    line("[Ontology]", "CL")
    line("Disease", "Systemic Lupus Erythematosus")
    line("[Ontology]", "EFO")
    line("Experiment type", "Single donor")
    line()

    line("Experiment[]", "Title", "Type")
    line("EXP-001", "Experiment 1", "Imaging")
    line("EXP-002", "Experiment 2", "Restoring")
    line()

//    line("File", "plates/J_Sero_plate_keys.xlsx")
//    line("Description", "Summary data")
//    line("Type", "XLSX File")
//    line()

//    line("Files", "Description", "Type")
//    line("plates/J_Sero_plate_keys.xlsx", "Summary data", "Library File")
//    line("plates/Plate01.csv", "Data for Plate 01", "Plate Details File")
//    line()

    line("Link", "IHECRE00000919.1")
    line("Type", "EpiRR")
    line()

    line("Stranded Total RNA-Seq", "SUBSECT-001", "SECT-001")
    line()

    line("Data[SUBSECT-001]", "Title", "Description")
    line("DT-1", "Group 1 Transcription Data", "The data for zygotic transcription in mammals group 1")
    line("DT-2", "Group 2 Transcription Data", "The data for zygotic transcription in mammals group 2")
    line()


    line("Links", "Type", "Assay type")
    line("EGAD00001001282", "EGA", "RNA-Seq")
    line()
}
