package ac.uk.ebi.biostd.itest.factory

import com.jayway.jsonpath.matchers.JsonPathMatchers.isJson
import com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

fun allInOneSubmissionJson(accNo: String) = jsonObj {
    "accno" to accNo
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

fun assertAllInOneSubmissionJson(json: String, accNo: String) {
    assertThat(json, isJson(withJsonPath("$.accno", equalTo(accNo))))
    assertThat(json, isJson(withJsonPath("$.attributes[0].name", equalTo("Title"))))
    assertThat(json, isJson(withJsonPath("$.attributes[0].value", equalTo("venous blood, Monocyte"))))
    assertThat(json, isJson(withJsonPath("$.attributes[1].name", equalTo("ReleaseDate"))))
    assertThat(json, isJson(withJsonPath("$.attributes[1].value", equalTo("2021-02-12"))))

    val section = "$.section"
    assertThat(json, isJson(withJsonPath("$section.accno", equalTo("SECT-001"))))
    assertThat(json, isJson(withJsonPath("$section.type", equalTo("Study"))))
    assertThat(json, isJson(withJsonPath("$section.attributes[0].name", equalTo("Project"))))
    assertThat(json, isJson(withJsonPath("$section.attributes[0].value", equalTo("CEEHRC (McGill)"))))
    assertThat(json, isJson(withJsonPath("$section.attributes[1].name", equalTo("Organization"))))
    assertThat(json, isJson(withJsonPath("$section.attributes[1].value", equalTo("Org1"))))
    assertThat(json, isJson(withJsonPath("$section.attributes[1].reference", equalTo(true))))

    val sectionSecondAttribute = "$.section.attributes[2]"
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.name", equalTo("Tissue type"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.value", equalTo("venous blood"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.valqual[0].name", equalTo("Ontology"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.valqual[0].value", equalTo("UBERON"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.nmqual[0].name", equalTo("Tissue"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.nmqual[0].value", equalTo("Blood"))))

    assertThat(json, isJson(withJsonPath("$.section.links[0].url", equalTo("AF069309"))))
    assertThat(json, isJson(withJsonPath("$.section.links[0].attributes[0].name", equalTo("type"))))
    assertThat(json, isJson(withJsonPath("$.section.links[0].attributes[0].value", equalTo("gen"))))

    val subsection = "$.section.subsections[0]"
    assertThat(json, isJson(withJsonPath("$subsection.accno", equalTo("SUBSECT-001"))))
    assertThat(json, isJson(withJsonPath("$subsection.type", equalTo("Stranded Total RNA-Seq"))))

    val linkTable = "$subsection.links[0]"
    val tableLink = "$linkTable[0]"
    assertThat(json, isJson(withJsonPath("$tableLink.url", equalTo("EGAD00001001282"))))
    assertThat(json, isJson(withJsonPath("$tableLink.attributes[0].name", equalTo("Type"))))
    assertThat(json, isJson(withJsonPath("$tableLink.attributes[0].value", equalTo("EGA"))))
    assertThat(json, isJson(withJsonPath("$tableLink.attributes[1].name", equalTo("Assay type"))))
    assertThat(json, isJson(withJsonPath("$tableLink.attributes[1].value", equalTo("RNA-Seq"))))

    val subsectionTable = "$.section.subsections[1]"
    val tableSection = "$subsectionTable[0]"
    assertThat(json, isJson(withJsonPath("$tableSection.accno", equalTo("DT-1"))))
    assertThat(json, isJson(withJsonPath("$tableSection.type", equalTo("Data"))))
    assertThat(json, isJson(withJsonPath("$tableSection.attributes[0].name", equalTo("Title"))))
    assertThat(json, isJson(withJsonPath("$tableSection.attributes[0].value", equalTo("Group 1 Transcription Data"))))
    assertThat(json, isJson(withJsonPath("$tableSection.attributes[1].name", equalTo("Description"))))
    assertThat(json, isJson(withJsonPath("$tableSection.attributes[1].value", equalTo("The data for zygotic transcription in mammals group 1"))))
}
