package ac.uk.ebi.biostd.itest.factory

import com.jayway.jsonpath.matchers.JsonPathMatchers.isJson
import com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.util.date.toStringDate
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import java.time.OffsetDateTime.now

internal val expectedAllInOneJsonFileList = jsonArray(
    {
        "path" to "DataFile5.txt"
        "size" to 9
        "attributes" to jsonArray(
            {
                "name" to "Type"
                "value" to "referenced"
            }
        )
        "type" to "file"
    },
    {
        "path" to "Folder1/DataFile6.txt"
        "size" to 9
        "attributes" to jsonArray(
            {
                "name" to "Type"
                "value" to "referenced"
            }
        )
        "type" to "file"
    }
)

internal val expectedAllInOneJsonInnerFileList = jsonArray(
    {
        "path" to "DataFile7.txt"
        "size" to 9
        "attributes" to jsonArray(
            {
                "name" to "Type"
                "value" to "referenced"
            }
        )
        "type" to "file"
    },
    {
        "path" to "Folder1"
        "size" to 160
        "attributes" to jsonArray(
            {
                "name" to "Type"
                "value" to "referenced directory"
            }
        )
        "type" to "directory"
    },
)

fun assertAllInOneSubmissionJson(json: String, accNo: String) {
    assertThat(json, isJson(withJsonPath("$.accno", equalTo(accNo))))
    assertJsonAttributes(
        json, "$", listOf(Attribute("Title", "venous blood, ∆Monocyte"), Attribute("ReleaseDate", now().toStringDate()))
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
    assertJsonSubSection(json, "$.section.subsections[0]", allInOneSubsection())
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

private fun assertJsonSubSection(json: String, path: String, section: Section) {
    assertThat(json, isJson(withJsonPath("$path.accno", equalTo(section.accNo))))
    assertThat(json, isJson(withJsonPath("$path.type", equalTo(section.type))))
    val attribute = "$path.attributes[0]"
    assertThat(json, isJson(withJsonPath("$attribute.name", equalTo("File List"))))
    assertThat(json, isJson(withJsonPath("$attribute.value", equalTo("sub-folder/file-list2.json"))))
}

private fun assertJsonLinksTable(json: String, path: String, linksTable: LinksTable) =
    linksTable.elements.forEachIndexed { idx, link -> assertJsonLink(json, "$path[$idx]", link) }

private fun assertJsonLink(json: String, path: String, link: Link) {
    assertThat(json, isJson(withJsonPath("$path.url", equalTo(link.url))))
    assertJsonAttributes(json, path, link.attributes)
}

private fun assertJsonFilesTable(json: String, path: String, filesTable: FilesTable) =
    filesTable.elements.forEachIndexed { idx, file -> assertJsonFile(json, "$path[$idx]", file) }

private fun assertJsonFile(json: String, path: String, file: BioFile) {
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
