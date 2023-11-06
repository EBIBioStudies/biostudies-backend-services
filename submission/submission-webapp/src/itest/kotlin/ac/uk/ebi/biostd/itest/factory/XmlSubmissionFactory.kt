package ac.uk.ebi.biostd.itest.factory

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
import org.redundent.kotlin.xml.xml
import org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath
import java.time.OffsetDateTime.now

internal val expectedAllInOneXmlFileList = xml("table") {
    "file" {
        attribute("size", 9)
        "path" { -"DataFile5.txt" }
        "type" { -"file" }
        "attributes" {
            "attribute" {
                "name" { -"Type" }
                "value" { -"referenced" }
            }
        }
    }
    "file" {
        attribute("size", 9)
        "path" { -"Folder1/DataFile6.txt" }
        "type" { -"file" }
        "attributes" {
            "attribute" {
                "name" { -"Type" }
                "value" { -"referenced" }
            }
        }
    }
}

internal val expectedAllInOneXmlInnerFileList = xml("table") {
    "file" {
        attribute("size", 9)
        "path" { -"DataFile7.txt" }
        "type" { -"file" }
        "attributes" {
            "attribute" {
                "name" { -"Type" }
                "value" { -"referenced" }
            }
        }
    }
    "file" {
        attribute("size", 160)
        "path" { -"Folder1" }
        "type" { -"directory" }
        "attributes" {
            "attribute" {
                "name" { -"Type" }
                "value" { -"referenced directory" }
            }
        }
    }
}

fun assertAllInOneSubmissionXml(xml: String, accNo: String) {
    assertThat(xml, hasXPath("//submission/@accno", equalTo(accNo)))
    assertXmlAttributes(
        xml,
        "//submission",
        listOf(Attribute("Title", "venous blood, ∆Monocyte"), Attribute("ReleaseDate", now().toStringDate()))
    )

    assertXmlSection(xml, "//submission/section", allInOneRootSection())
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[2]/@reference", equalTo("true")))
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[3]/nmqual/name", equalTo("Tissue")))
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[3]/nmqual/value", equalTo("Blood")))
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[3]/valqual/name", equalTo("Ontology")))
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[3]/valqual/value", equalTo("UBERON")))
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[4]/name", equalTo("File List")))
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[4]/value", equalTo("file-list.xml")))

    assertXmlLink(xml, "//submission/section/links/link", allInOneRootSectionLink())
    assertXmlFile(xml, "//submission/section/files/file[1]", allInOneRootSectionFile())
    assertXmlFilesTable(xml, "//submission/section/files/table", allInOneRootSectionFilesTable())

    assertXmlSubSection(xml, "//submission/section/subsections/section", allInOneSubsection())
    assertXmlLinksTable(xml, "//submission/section/subsections/section/links/table", allInOneSubSectionLinksTable())
    assertXmlSectionsTable(xml, "//submission/section/subsections/table", allInOneSubSectionsTable())
}

private fun assertXmlSection(xml: String, xPath: String, section: Section) {
    assertThat(xml, hasXPath("$xPath/@accno", equalTo(section.accNo)))
    assertThat(xml, hasXPath("$xPath/@type", equalTo(section.type)))
    assertXmlAttributes(xml, xPath, section.attributes)
}

private fun assertXmlSubSection(xml: String, xPath: String, section: Section) {
    assertThat(xml, hasXPath("$xPath/@accno", equalTo(section.accNo)))
    assertThat(xml, hasXPath("$xPath/@type", equalTo(section.type)))
    val attribute = "$xPath/attributes/attribute[1]"
    assertThat(xml, hasXPath("$attribute/name", equalTo("File List")))
    assertThat(xml, hasXPath("$attribute/value", equalTo("sub-folder/file-list2.xml")))
}

private fun assertXmlSectionsTable(xml: String, xPath: String, sectionsTable: SectionsTable) =
    sectionsTable.elements.forEachIndexed { idx, section ->
        assertXmlSection(xml, "$xPath/section[${idx + 1}]", section)
    }

private fun assertXmlLink(xml: String, xPath: String, link: Link) {
    assertThat(xml, hasXPath("$xPath/url", equalTo(link.url)))
    assertXmlAttributes(xml, xPath, link.attributes)
}

private fun assertXmlLinksTable(xml: String, xPath: String, linksTable: LinksTable) =
    linksTable.elements.forEachIndexed { idx, link -> assertXmlLink(xml, "$xPath/link[${idx + 1}]", link) }

private fun assertXmlFile(xml: String, xPath: String, file: BioFile) {
    assertThat(xml, hasXPath("$xPath/path", equalTo(file.path)))
    assertThat(xml, hasXPath("$xPath/@size", equalTo(file.size.toString())))
    assertXmlAttributes(xml, xPath, file.attributes)
}

private fun assertXmlFilesTable(xml: String, xPath: String, filesTable: FilesTable) =
    filesTable.elements.forEachIndexed { idx, file -> assertXmlFile(xml, "$xPath/file[${idx + 1}]", file) }

private fun assertXmlAttributes(xml: String, xPath: String, attributes: List<Attribute>) =
    attributes.forEachIndexed { idx, attr ->
        assertThat(xml, hasXPath("$xPath/attributes/attribute[${idx + 1}]/name", equalTo(attr.name)))
        assertThat(xml, hasXPath("$xPath/attributes/attribute[${idx + 1}]/value", equalTo(attr.value)))
    }
