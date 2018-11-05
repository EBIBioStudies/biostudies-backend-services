package ac.uk.ebi.biostd.test

import ac.uk.ebi.biostd.tsv.Tsv
import ac.uk.ebi.biostd.tsv.line
import ac.uk.ebi.biostd.tsv.tsv

fun basicSubmission(): Tsv {
    return tsv {
        line("Submission", "S-EPMC123")
        line("Title", "Basic Submission")
        line("DataSource", "EuropePMC")
        line("AttachTo", "EuropePMC")
        line()
    }
}

fun submissionWithDetailedAttributes(): Tsv {
    return tsv {
        line("Submission", "S-EPMC124")
        line("Title", "Submission With Detailed Attributes")
        line("Submission Type", "RNA-seq of non coding RNA")
        line("(Seq Type)", "RNA")
        line("[Ontology]", "EFO")
        line("<affiliation>", "EuropePMC")
        line()
    }
}

fun submissionWithRootSection(): Tsv {
    return tsv {
        line("Submission", "S-EPMC125")
        line("Title", "Test Submission")
        line()

        line("Study")
        line("Title", "Test Root Section")
        line("Abstract", "Test abstract")
        line()
    }
}

fun submissionWithSectionsTable(): Tsv {
    val pageTab = submissionWithRootSection()
    pageTab.line("Data", "Title", "Desc")
    pageTab.line("DT-1", "Data 1", "Group 1")
    pageTab.line("DT-1", "Data 2", "Group 2")
    pageTab.line()

    return pageTab
}

fun submissionWithSubsection(): Tsv {
    val pageTab = submissionWithRootSection()
    pageTab.line("Funding")
    pageTab.line("Agency", "National Support Program of China")
    pageTab.line("Grant Id", "No. 2015BAD27B01")
    pageTab.line()

    return pageTab
}

fun submissionWithLinks(): Tsv {
    val pageTab = submissionWithRootSection()
    pageTab.line("Link", "http://arandomsite.org")
    pageTab.line()
    pageTab.line("Link", "http://completelyunrelatedsite.org")
    pageTab.line()

    return pageTab
}

fun submissionWithLinksTable(): Tsv {
    val pageTab = submissionWithRootSection()
    pageTab.line("Links", "Type")
    pageTab.line("AF069309", "gen")
    pageTab.line("AF069123", "gen")
    pageTab.line()

    return pageTab
}

fun submissionWithFiles(): Tsv {
    val pageTab = submissionWithRootSection()
    pageTab.line("File", "12870_2017_1225_MOESM10_ESM.docx")
    pageTab.line()
    pageTab.line("File", "12870_2017_1225_MOESM1_ESM.docx")
    pageTab.line()

    return pageTab
}


fun submissionWithFilesTable(): Tsv {
    val pageTab = submissionWithRootSection()
    pageTab.line("Files", "Description", "Usage")
    pageTab.line("Abstract.pdf", "An abstract file", "Testing")
    pageTab.line("SuperImportantFile1.docx", "A super important file", "Important stuff")
    pageTab.line()

    return pageTab
}
