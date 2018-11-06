package ac.uk.ebi.biostd.test

import ac.uk.ebi.biostd.tsv.line
import ac.uk.ebi.biostd.tsv.tsv

fun basicSubmission() = tsv {
    line("Submission", "S-EPMC123")
    line("Title", "Basic Submission")
    line("DataSource", "EuropePMC")
    line("AttachTo", "EuropePMC")
    line()
}

fun submissionWithDetailedAttributes() = tsv {
    line("Submission", "S-EPMC124")
    line("Title", "Submission With Detailed Attributes")
    line("Submission Type", "RNA-seq of non coding RNA")
    line("(Seq Type)", "RNA")
    line("[Ontology]", "EFO")
    line("<affiliation>", "EuropePMC")
    line()
}

fun submissionWithRootSection() = tsv {
    line("Submission", "S-EPMC125")
    line("Title", "Test Submission")
    line()

    line("Study")
    line("Title", "Test Root Section")
    line("Abstract", "Test abstract")
    line()
}

fun submissionWithSectionsTable() = submissionWithRootSection().apply {
    line("Data", "Title", "Desc")
    line("DT-1", "Data 1", "Group 1")
    line("DT-1", "Data 2", "Group 2")
    line()
}

fun submissionWithSubsection() = submissionWithRootSection().apply {
    line("Funding")
    line("Agency", "National Support Program of China")
    line("Grant Id", "No. 2015BAD27B01")
    line()
}

fun submissionWithLinks() = submissionWithRootSection().apply {
    line("Link", "http://arandomsite.org")
    line()
    line("Link", "http://completelyunrelatedsite.org")
    line()
}

fun submissionWithLinksTable() = submissionWithRootSection().apply {
    line("Links", "Type")
    line("AF069309", "gen")
    line("AF069123", "gen")
    line()
}

fun submissionWithFiles() = submissionWithRootSection().apply {
    line("File", "12870_2017_1225_MOESM10_ESM.docx")
    line()
    line("File", "12870_2017_1225_MOESM1_ESM.docx")
    line()
}


fun submissionWithFilesTable() = submissionWithRootSection().apply {
    line("Files", "Description", "Usage")
    line("Abstract.pdf", "An abstract file", "Testing")
    line("SuperImportantFile1.docx", "A super important file", "Important stuff")
    line()
}
