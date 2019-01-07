package ac.uk.ebi.biostd.itest.factory

import ebi.ac.uk.dsl.jsonArray
import ebi.ac.uk.dsl.jsonObj
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.tsv

fun allInOneSubmissionJson() = jsonObj {
    "accNo" to "S-EPMC125"
    "attributes" to jsonArray({
        "name" to "Title"
        "value" to "venous blood, Monocyte"
    })
    "section" to {
        "accNo" to "SECT-001"
        "type" to "Study"
        "attributes" to jsonArray({
            "name" to "Project"
            "value" to "CEEHRC (McGill)"
        }, {
            "name" to "Organization"
            "value" to "Org1"
            "reference" to true
        }, {
            "name" to "Tissue type"
            "value" to "venous blood"
            "valqual" to jsonArray({
                "name" to "Ontology"
                "value" to "UBERON"
            })
            "namequal" to jsonArray({
                "name" to "Tissue"
                "value" to "Blood"
            })
        })
        "links" to jsonArray({
            "url" to "AF069309"
            "attributes" to jsonArray({
                "name" to "type"
                "value" to "gen"
            })
        })
        "files" to jsonArray(
            jsonObj {
                "path" to "LibraryFile1.txt"
                "attributes" to jsonArray({
                    "name" to "description"
                    "value" to "Library File 1"
                })
            },
            jsonArray({
                "path" to "LibraryFile2.txt"
                "attributes" to jsonArray({
                    "name" to "description"
                    "value" to "Library File 2"
                }, {
                    "name" to "type"
                    "value" to "Lib"
                })
            }))
        "subsections" to jsonArray(jsonObj {
            "accNo" to "SUBSECT-001"
            "type" to "Stranded Total RNA-Seq"
            "links" to jsonArray(jsonArray({
                "url" to "EGAD00001001282"
                "attributes" to jsonArray({
                    "name" to "type"
                    "value" to "EGA"
                }, {
                    "name" to "Assay type"
                    "value" to "RNA-Seq"
                })
            }))
        }, jsonArray({
            "accNo" to "DT-1"
            "type" to "Data"
            "attributes" to jsonArray({
                "name" to "Title"
                "value" to "Group 1 Transcription Data"
            }, {
                "name" to "Description"
                "value" to "The data for zygotic transcription in mammals group 1"
            })
        }))
    }
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

    line("File", "LibraryFile1.txt")
    line("Description", "Library File 1")
    line()

    line("Files", "Description", "Type")
    line("LibraryFile2.txt", "Library File 2", "Lib")
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
