package ac.uk.ebi.biostd.itest.factory

import ac.uk.ebi.biostd.itest.assertions.SubmissionSpec
import com.jayway.jsonpath.matchers.JsonPathMatchers.isJson
import com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

fun submissionSpecJson(accNo: String) = SubmissionSpec(allInOneSubmissionJson(accNo).toString(), fileList().toString())

fun allInOneSubmissionJson(accNo: String) = jsonObj {
    "accno" to accNo
    "attributes" to jsonArray(
        {
            "name" to "Title"
            "value" to "venous blood, ∆Monocyte"
        },
        {
            "name" to "ReleaseDate"
            "value" to "2021-02-12"
        }
    )
    "section" to {
        "accno" to "SECT-001"
        "type" to "Study"
        "attributes" to jsonArray(
            {
                "name" to "Project"
                "value" to "CEEHRC (McGill)"
            },
            {
                "name" to "Organization"
                "value" to "Org1"
                "reference" to true
            },
            {
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
            },
            {
                "name" to "File List"
                "value" to "file-list.json"
            }
        )
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
            jsonArray(
                {
                    "path" to "DataFile2.txt"
                    "attributes" to jsonArray(
                        {
                            "name" to "Description"
                            "value" to "Data File 2"
                        },
                        {
                            "name" to "Type"
                            "value" to "Data"
                        }
                    )
                },
                {
                    "path" to "Folder1/DataFile3.txt"
                    "attributes" to jsonArray(
                        {
                            "name" to "Description"
                            "value" to "Data File 3"
                        },
                        {
                            "name" to "Type"
                            "value" to "Data"
                        }
                    )
                },
                {
                    "path" to "Folder1/Folder2/DataFile4.txt"
                    "attributes" to jsonArray(
                        {
                            "name" to "Description"
                            "value" to "Data File 4"
                        },
                        {
                            "name" to "Type"
                            "value" to "Data"
                        }
                    )
                }
            )
        )
        "subsections" to jsonArray(
            jsonObj {
                "accno" to "SUBSECT-001"
                "type" to "Stranded Total RNA-Seq"
                "links" to jsonArray(
                    jsonArray({
                        "url" to "EGAD00001001282"
                        "attributes" to jsonArray(
                            {
                                "name" to "Type"
                                "value" to "EGA"
                            },
                            {
                                "name" to "Assay type"
                                "value" to "RNA-Seq"
                                "valqual" to jsonArray({
                                    "name" to "Ontology"
                                    "value" to "EFO"
                                })
                                "nmqual" to jsonArray({
                                    "name" to "TermId"
                                    "value" to "EFO_0002768"
                                })
                            }
                        )
                    })
                )
            },
            jsonArray({
                "accno" to "DT-1"
                "type" to "Data"
                "attributes" to jsonArray(
                    {
                        "name" to "Title"
                        "value" to "Group 1 Transcription Data"
                    },
                    {
                        "name" to "Description"
                        "value" to "The data for zygotic transcription in mammals group 1"
                    }
                )
            })
        )
    }
}

private fun fileList() = jsonArray(
    {
        "path" to "DataFile5.txt"
        "attributes" to jsonArray(
            {
                "name" to "Type"
                "value" to "referenced"
            }
        )
    },
    {
        "path" to "Folder1/DataFile6.txt"
        "attributes" to jsonArray(
            {
                "name" to "Type"
                "value" to "referenced"
            }
        )
    }
)

fun assertAllInOneSubmissionJson(json: String, accNo: String) {
    assertThat(json, isJson(withJsonPath("$.accno", equalTo(accNo))))
    assertJsonAttributes(
        json, "$", listOf(Attribute("Title", "venous blood, ∆Monocyte"), Attribute("ReleaseDate", "2021-02-12"))
    )

    val section = allInOneRootSection()
    assertJsonSection(json, "$.section", section)
    assertThat(json, isJson(withJsonPath("$.section.attributes[1].reference", equalTo(true))))

    val sectionSecondAttribute = "$.section.attributes[2]"
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.valqual[0].name", equalTo("Ontology"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.valqual[0].value", equalTo("UBERON"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.nmqual[0].name", equalTo("Tissue"))))
    assertThat(json, isJson(withJsonPath("$sectionSecondAttribute.nmqual[0].value", equalTo("Blood"))))

    val sectionThirdAttribute = "$.section.attributes[3]"
    assertThat(json, isJson(withJsonPath("$sectionThirdAttribute.name", equalTo("File List"))))
    assertThat(json, isJson(withJsonPath("$sectionThirdAttribute.value", equalTo("file-list.json"))))

    assertJsonLink(json, "$.section.links[0]", allInOneRootSectionLink())
    assertJsonFile(json, "$.section.files[0]", allInOneRootSectionFile())
    assertJsonFilesTable(json, "$.section.files[1]", allInOneRootSectionFilesTable())
    assertJsonSection(json, "$.section.subsections[0]", allInOneSubsection())
    assertJsonLinksTable(json, "$.section.subsections[0].links[0]", allInOneSubSectionLinksTable())
    assertJsonSectionsTable(json, "$.section.subsections[1]", allInOneSubSectionsTable())
}

private fun assertJsonSectionsTable(json: String, path: String, sectionsTable: SectionsTable) =
    sectionsTable.elements.forEachIndexed { idx, section -> assertJsonSection(json, "$path[$idx]", section) }

private fun assertJsonSection(json: String, path: String, section: Section) {
    assertThat(json, isJson(withJsonPath("$path.accno", equalTo(section.accNo))))
    assertThat(json, isJson(withJsonPath("$path.type", equalTo(section.type))))
    assertJsonAttributes(json, path, section.attributes)
}

private fun assertJsonLinksTable(json: String, path: String, linksTable: LinksTable) =
    linksTable.elements.forEachIndexed { idx, link -> assertJsonLink(json, "$path[$idx]", link) }

private fun assertJsonLink(json: String, path: String, link: Link) {
    assertThat(json, isJson(withJsonPath("$path.url", equalTo(link.url))))
    assertJsonAttributes(json, path, link.attributes)
}

private fun assertJsonFilesTable(json: String, path: String, filesTable: FilesTable) =
    filesTable.elements.forEachIndexed { idx, file -> assertJsonFile(json, "$path[$idx]", file) }

private fun assertJsonFile(json: String, path: String, file: File) {
    assertThat(json, isJson(withJsonPath("$path.type", equalTo("file"))))
    assertThat(json, isJson(withJsonPath("$path.path", equalTo(file.path))))
    assertThat(json, isJson(withJsonPath("$path.size", equalTo(file.size.toInt()))))

    assertJsonAttributes(json, path, file.attributes)
}

private fun assertJsonAttributes(json: String, path: String, attributes: List<Attribute> = emptyList()) =
    attributes.forEachIndexed { idx, attr ->
        val attributePath = "$path.attributes[$idx]"
        assertThat(json, isJson(withJsonPath("$attributePath.name", equalTo(attr.name))))
        assertThat(json, isJson(withJsonPath("$attributePath.value", equalTo(attr.value))))
    }
