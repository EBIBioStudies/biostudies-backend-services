package ac.uk.ebi.biostd.itest.factory

import com.jayway.jsonpath.matchers.JsonPathMatchers.isJson
import com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.link
import ebi.ac.uk.dsl.section
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.extensions.type
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
                    "name" to "Description"
                    "value" to "Data File 1"
                })
            },
            jsonArray({
                "path" to "DataFile2.txt"
                "attributes" to jsonArray({
                    "name" to "Description"
                    "value" to "Data File 2"
                }, {
                    "name" to "Type"
                    "value" to "Data"
                })
            }, {
                "path" to "Folder1/DataFile3.txt"
                "attributes" to jsonArray({
                    "name" to "Description"
                    "value" to "Data File 3"
                }, {
                    "name" to "Type"
                    "value" to "Data"
                })
            }, {
                "path" to "Folder1/Folder2/DataFile4.txt"
                "attributes" to jsonArray({
                    "name" to "Description"
                    "value" to "Data File 4"
                }, {
                    "name" to "Type"
                    "value" to "Data"
                })
            }))
        "subsections" to jsonArray(jsonObj {
            "accno" to "SUBSECT-001"
            "type" to "Stranded Total RNA-Seq"
            "links" to jsonArray(jsonArray({
                "url" to "EGAD00001001282"
                "attributes" to jsonArray({
                    "name" to "Type"
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
    assertJsonAttributes(
        json, "$", listOf(Attribute("Title", "venous blood, Monocyte"), Attribute("ReleaseDate", "2021-02-12")))
//    assertThat(json, isJson(withJsonPath("$.attributes[0].name", equalTo("Title"))))
//    assertThat(json, isJson(withJsonPath("$.attributes[0].value", equalTo("venous blood, Monocyte"))))
//    assertThat(json, isJson(withJsonPath("$.attributes[1].name", equalTo("ReleaseDate"))))
//    assertThat(json, isJson(withJsonPath("$.attributes[1].value", equalTo("2021-02-12"))))

//    val section = "$.section"
    val section = section("Study") {
        this.accNo = "SECT-001"

        attribute("Project", "CEEHRC (McGill)")
        attribute("Organization", "Org1")
        attribute("Tissue type", "venous blood")
    }

    assertJsonSection(json, "$.section", section)
//    assertThat(json, isJson(withJsonPath("$section.accno", equalTo("SECT-001"))))
//    assertThat(json, isJson(withJsonPath("$section.type", equalTo("Study"))))
//    assertJsonAttributes(
//        json,
//        section,
//        listOf(
//            Attribute("Project", "CEEHRC (McGill)"),
//            Attribute("Organization", "Org1"),
//            Attribute("Tissue type", "venous blood")))
//    assertThat(json, isJson(withJsonPath("$section.attributes[0].name", equalTo("Project"))))
//    assertThat(json, isJson(withJsonPath("$section.attributes[0].value", equalTo("CEEHRC (McGill)"))))
//    assertThat(json, isJson(withJsonPath("$section.attributes[1].name", equalTo("Organization"))))
//    assertThat(json, isJson(withJsonPath("$section.attributes[1].value", equalTo("Org1"))))
    assertThat(json, isJson(withJsonPath("$section.attributes[1].reference", equalTo(true))))

    val sectionSecondAttribute = "$.section.attributes[2]"
//    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.name", equalTo("Tissue type"))))
//    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.value", equalTo("venous blood"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.valqual[0].name", equalTo("Ontology"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.valqual[0].value", equalTo("UBERON"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.nmqual[0].name", equalTo("Tissue"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.nmqual[0].value", equalTo("Blood"))))

//    val sectionLink = "$.section.links[0]"
    val sectionLink = link("AF069309"){
        attribute("type", "gen")
    }
    assertJsonLink(json, "$.section.links[0]", sectionLink)
//    assertThat(json, isJson(withJsonPath("$sectionLink.url", equalTo("AF069309"))))
//    assertJsonAttributes(json, sectionLink, listOf(Attribute("type", "gen")))
//    assertThat(json, isJson(withJsonPath("$.section.links[0].attributes[0].name", equalTo("type"))))
//    assertThat(json, isJson(withJsonPath("$.section.links[0].attributes[0].value", equalTo("gen"))))

//    val sectionFile = "$.section.files[0]"
    val sectionFile = file("DataFile2.txt") {
        size = 0
        type = "file"
        attribute("Description", "Data File 2")
        attribute("Type", "Data")
    }
    assertJsonFile(json, "$.section.files[0]", sectionFile)
//    assertThat(json, isJson(withJsonPath("$sectionFile.path", equalTo("DataFile1.txt"))))
//    assertThat(json, isJson(withJsonPath("$sectionFile.type", equalTo("file"))))
//    assertThat(json, isJson(withJsonPath("$sectionFile.size", equalTo(0))))
//    assertThat(json, isJson(withJsonPath("$sectionFile.attributes[0].name", equalTo("Description"))))
//    assertThat(json, isJson(withJsonPath("$sectionFile.attributes[0].value", equalTo("Data File 1"))))

    val sectionFilesTable = "$.section.files[1]"
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[0].path", equalTo("DataFile2.txt"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[0].type", equalTo("file"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[0].size", equalTo(0))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[0]attributes[0].name", equalTo("Description"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[0]attributes[0].value", equalTo("Data File 2"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[0]attributes[1].name", equalTo("Type"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[0]attributes[1].value", equalTo("Data"))))

    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[1].path", equalTo("Folder1/DataFile3.txt"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[1].type", equalTo("file"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[1].size", equalTo(0))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[1]attributes[0].name", equalTo("Description"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[1]attributes[0].value", equalTo("Data File 3"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[1]attributes[1].name", equalTo("Type"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[1]attributes[1].value", equalTo("Data"))))

    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[2].path", equalTo("Folder1/Folder2/DataFile4.txt"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[2].type", equalTo("file"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[2].size", equalTo(0))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[2]attributes[0].name", equalTo("Description"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[2]attributes[0].value", equalTo("Data File 4"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[2]attributes[1].name", equalTo("Type"))))
    assertThat(json, isJson(withJsonPath("$sectionFilesTable.[2]attributes[1].value", equalTo("Data"))))

//    val subsection = "$.section.subsections[0]"
    val subsection = section("Stranded Total RNA-Seq") { this.accNo = "SUBSECT-001" }
    assertJsonSection(json, "$.section.subsections[0]", subsection)
    assertThat(json, isJson(withJsonPath("$subsection.accno", equalTo("SUBSECT-001"))))
    assertThat(json, isJson(withJsonPath("$subsection.type", equalTo("Stranded Total RNA-Seq"))))

    val subSectionLinksTable = "$subsection.links[0]"
    val tableLink = "$subSectionLinksTable[0]"
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

private fun assertJsonSection(json: String, path: String, section: Section) {
    assertThat(json, isJson(withJsonPath("$path.accno", equalTo(section.accNo))))
    assertThat(json, isJson(withJsonPath("$path.type", equalTo(section.type))))
    assertJsonAttributes(json, path, section.attributes)
}

private fun assertJsonLink(json: String, path: String, link: Link) {
    assertThat(json, isJson(withJsonPath("$path.url", equalTo(link.url))))
    assertJsonAttributes(json, path, link.attributes)
}

private fun assertJsonFile(json: String, path: String, file: File) {
    assertThat(json, isJson(withJsonPath("$path.path", equalTo(file.path))))
    assertThat(json, isJson(withJsonPath("$path.type", equalTo(file.type))))
    assertThat(json, isJson(withJsonPath("$path.size", equalTo(file.size))))
    assertJsonAttributes(json, path, file.attributes)
}

private fun assertJsonAttributes(json: String, path: String, attributes: List<Attribute> = emptyList()) =
    attributes.forEachIndexed { idx, attr ->
        val attributePath = "$path.attributes[$idx]"
        assertThat(json, isJson(withJsonPath("$attributePath.name", equalTo(attr.name))))
        assertThat(json, isJson(withJsonPath("$attributePath.value", equalTo(attr.value))))
    }

//        attr.nameAttrs.forEachIndexed { nameIdx, nameAttr ->
//            assertThat(json, isJson(withJsonPath("$attributePath.nmqual[$nameIdx].name", equalTo(nameAttr.name))))
//            assertThat(json, isJson(withJsonPath("$attributePath.nmqual[$nameIdx].value", equalTo(nameAttr.value))))
//        }
//
//        attr.valueAttrs.forEachIndexed { valIdx, valAttr ->
//            assertThat(json, isJson(withJsonPath("$attributePath.nmqual[$valIdx].name", equalTo(valAttr.name))))
//            assertThat(json, isJson(withJsonPath("$attributePath.nmqual[$valIdx].value", equalTo(valAttr.value))))
//        }
//    }
//}
