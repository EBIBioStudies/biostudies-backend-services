package ac.uk.ebi.biostd.itest.factory

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj

fun allInOneSubmissionJson() = jsonObj {
    "accno" to "S-EPMC125"
    "attributes" to jsonArray({
        "name" to "Title"
        "value" to "venous blood, Monocyte"
    }, {
        "name" to "ReleaseDate"
        "value" to "2021-02-12"
    })
    "section" to {
        "accno" to "SECT-001"
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
            "nmqual" to jsonArray({
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
                "path" to "DataFile1.txt"
                "attributes" to jsonArray({
                    "name" to "description"
                    "value" to "Data File 1"
                })
            },
            jsonArray({
                "path" to "DataFile2.txt"
                "attributes" to jsonArray({
                    "name" to "description"
                    "value" to "Data File 2"
                }, {
                    "name" to "type"
                    "value" to "Data"
                })
            }, {
                "path" to "Folder1/DataFile3.txt"
                "attributes" to jsonArray({
                    "name" to "description"
                    "value" to "Data File 3"
                }, {
                    "name" to "type"
                    "value" to "Data"
                })
            }, {
                "path" to "Folder1/Folder2/DataFile4.txt"
                "attributes" to jsonArray({
                    "name" to "description"
                    "value" to "Data File 4"
                }, {
                    "name" to "type"
                    "value" to "Data"
                })
            }))
        "subsections" to jsonArray(jsonObj {
            "accno" to "SUBSECT-001"
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
            "accno" to "DT-1"
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
