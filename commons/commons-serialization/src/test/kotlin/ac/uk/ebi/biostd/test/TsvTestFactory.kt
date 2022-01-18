package ac.uk.ebi.biostd.test

import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv

fun basicSubmission() = tsv {
    line("Submission", "S-EPMC123")
    line("Title", "Basic Submission")
    line("DataSource", "EuropePMC")
    line("AttachTo", "EuropePMC")
    line()
}

fun submissionWithEmptyAttribute() = tsv {
    line("Submission", "S-EPMC123")
    line("Title", "Basic Submission")
    line("DataSource", "EuropePMC")
    line("Abstract")
    line()
}

fun submissionWithBlankAttribute() = tsv {
    line("Submission", "S-EPMC123")
    line("Title", "Basic Submission")
    line("DataSource", "EuropePMC")
    line("Abstract", "  ")
    line()
}

fun submissionWithNullAttribute() = tsv {
    line("Submission", "S-EPMC123")
    line("Title", "Basic Submission")
    line("DataSource", "EuropePMC")
    line("Abstract")
    line()
}

fun submissionWithQuoteValue() = tsv {
    line("Submission", "S-EPMC123")
    line("Title", "The \"Submission\": title.")
    line("Abstract", "\"The Submission\": this is description.")
    line("Sub-Title", "\"The Submission (quoted)\": this is description.")
    line("Double Quote Attribute", "\"one value\" OR \"the other\"")
    line()
}

fun basicSubmissionWithMultiline() = tsv {
    line("Submission", "S-EPMC123")
    line("Title", "\"This is a really long title \n with a break line\"")
}

fun basicSubmissionWithComments() = tsv {
    line("# This is a test submission")
    line("Submission", "S-EPMC123")
    line("Title", "Basic Submission")
    line("# This is a comment in the middle of the attrs")
    line("DataSource", "EuropePMC")
    line("AttachTo", "EuropePMC")
    line()

    line("# Yet another comment. It should be ignored")
}

fun submissionWithInvalidNameAttributeDetail() = tsv {
    line("Submission", "S-EPMC124")
    line("(Seq Type)", "RNA")
    line()
}

fun submissionWithInvalidValueAttributeDetail() = tsv {
    line("Submission", "S-EPMC124")
    line("[Ontology]", "EFO")
    line()
}

fun twoBasicSubmission() = basicSubmission().toString() + basicSubmission().toString()

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

fun submissionWithGenericRootSection() = tsv {
    line("Submission", "S-EPMC125")
    line("Title", "Test Submission")
    line()

    line("Compound")
    line("Title", "Generic Root Section")
    line()
}

fun submissionWithMultipleLineBreaks() = tsv {
    line("Submission", "S-EPMC125")
    line("Title", "Test Submission")
    line()
    line()
    line("", "")

    line("Study")
    line("Title", "Test Root Section")
    line("Abstract", "Test abstract")
    line()
}

fun submissionWithSectionsTable() = submissionWithRootSection().apply {
    line("Data[]", "Title", "Desc")
    line("DT-1", "", "Group 1")
    line("DT-2", "Data 2", "")
    line()
}

fun submissionWithSubsection() = submissionWithRootSection().apply {
    line("Funding", "F-001")
    line("Agency", "National Support Program of China")
    line("Grant Id", "No. 2015BAD27B01")
    line()
}

fun submissionWithInnerSubsections() = submissionWithSubsection().apply {
    line("Funding", "F-002")
    line("Agency", "National Support Program of Japan")
    line("Grant Id", "No. 2015BAD27A03")
    line()

    line("Expense", "E-001", "F-001")
    line("Description", "Travel")
    line()
}

fun submissionWithInvalidInnerSubsection() = submissionWithSubsection().apply {
    line("Expense", "E-001", "F-002")
    line("Description", "Travel")
    line()
}

fun submissionWithInnerSubsectionsTable() = submissionWithSubsection().apply {
    line("Study", "S-001")
    line("Type", "Imaging")
    line()

    line("Sample[S-001]", "Title", "Desc")
    line("SMP-1", "Sample1", "Measure 1")
    line("SMP-2", "Sample2", "Measure 2")
    line()
}

fun submissionWithLinks() = submissionWithRootSection().apply {
    line("Link", "http://arandomsite.org")
    line()
    line("Link", "http://completelyunrelatedsite.org")
    line()
}

fun submissionWithLinksTable() = submissionWithRootSection().apply {
    line("Links", "Type", "(TermId)", "[Ontology]")
    line("AF069309", "gen", "EFO_0002768", "EFO")
    line("AF069123", "gen", "EFO_0002769", "EFO")
    line()
}

fun submissionWithTableWithNoRows() = submissionWithRootSection().apply {
    line("Links", "Type")
    line()
}

fun submissionWithTableWithMoreAttributes() = submissionWithRootSection().apply {
    line("Links", "Type")
    line("AF069309", "gen", "RNA")
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
