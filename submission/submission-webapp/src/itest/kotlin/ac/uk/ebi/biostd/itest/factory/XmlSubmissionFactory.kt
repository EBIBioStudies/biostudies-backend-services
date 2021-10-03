package ac.uk.ebi.biostd.itest.factory

import ac.uk.ebi.biostd.itest.assertions.SubmissionSpec
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.redundent.kotlin.xml.xml
import org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath

fun submissionSpecXml(accNo: String) = SubmissionSpec(allInOneSubmissionXml(accNo).toString(), fileList().toString())

fun allInOneSubmissionXml(accNo: String) = xml("submission") {
    attribute("accno", accNo)
    "attributes" {
        "attribute" {
            "name" { -"Title" }
            "value" { -"venous blood, ∆Monocyte" }
        }
        "attribute" {
            "name" { -"ReleaseDate" }
            "value" { -"2021-02-12" }
        }
    }

    "section" {
        attribute("accno", "SECT-001")
        attribute("type", "Study")
        "attributes" {
            "attribute" {
                "name" { -"Project" }
                "value" { -"CEEHRC (McGill)" }
            }
            "attribute" {
                attribute("reference", true)
                "name" { -"Organization" }
                "value" { -"Org1" }
            }
            "attribute" {
                "name" { -"Tissue type" }
                "value" { -"venous blood" }
                "valqual" {
                    "name" { -"Ontology" }
                    "value" { -"UBERON" }
                }
                "nmqual" {
                    "name" { -"Tissue" }
                    "value" { -"Blood" }
                }
            }
            "attribute" {
                "name" { -"File List" }
                "value" { -"file-list.xml" }
            }
        }
        "links" {
            "link" {
                "url" { -"AF069309" }
                "attributes" {
                    "attribute" {
                        "name" { -"type" }
                        "value" { -"gen" }
                    }
                }
            }
        }
        "files" {
            "file" {
                "path" { -"DataFile1.txt" }
                "attributes" {
                    "attribute" {
                        "name" { -"Description" }
                        "value" { -"Data File 1" }
                    }
                }
            }
            "table" {
                "file" {
                    "path" { -"DataFile2.txt" }
                    "attributes" {
                        "attribute" {
                            "name" { -"Description" }
                            "value" { -"Data File 2" }
                        }
                        "attribute" {
                            "name" { -"Type" }
                            "value" { -"Data" }
                        }
                    }
                }
                "file" {
                    "path" { -"Folder1/DataFile3.txt" }
                    "attributes" {
                        "attribute" {
                            "name" { -"Description" }
                            "value" { -"Data File 3" }
                        }
                        "attribute" {
                            "name" { -"Type" }
                            "value" { -"Data" }
                        }
                    }
                }
                "file" {
                    "path" { -"Folder1/Folder2/DataFile4.txt" }
                    "attributes" {
                        "attribute" {
                            "name" { -"Description" }
                            "value" { -"Data File 4" }
                        }
                        "attribute" {
                            "name" { -"Type" }
                            "value" { -"Data" }
                        }
                    }
                }
            }
        }
        "subsections" {
            "section" {
                attribute("accno", "SUBSECT-001")
                attribute("type", "Stranded Total RNA-Seq")
                "links" {
                    "table" {
                        "link" {
                            "url" { -"EGAD00001001282" }
                            "attributes" {
                                "attribute" {
                                    "name" { -"Type" }
                                    "value" { -"EGA" }
                                }
                                "attribute" {
                                    "name" { -"Assay type" }
                                    "value" { -"RNA-Seq" }
                                    "valqual" {
                                        "name" { -"Ontology" }
                                        "value" { -"EFO" }
                                    }
                                    "nmqual" {
                                        "name" { -"TermId" }
                                        "value" { -"EFO_0002768" }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "table" {
                "section" {
                    attribute("accno", "DT-1")
                    attribute("type", "Data")
                    "attributes" {
                        "attribute" {
                            "name" { -"Title" }
                            "value" { -"Group 1 Transcription Data" }
                        }
                        "attribute" {
                            "name" { -"Description" }
                            "value" { -"The data for zygotic transcription in mammals group 1" }
                        }
                    }
                }
            }
        }
    }
}

private fun fileList() = xml("files") {
    "file" {
        "path" { -"DataFile5.txt" }
        "attributes" {
            "attribute" {
                "name" { -"Type" }
                "value" { -"referenced" }
            }
        }
    }
    "file" {
        "path" { -"Folder1/DataFile6.txt" }
        "attributes" {
            "attribute" {
                "name" { -"Type" }
                "value" { -"referenced" }
            }
        }
    }
}

fun assertAllInOneSubmissionXml(xml: String, accNo: String) {
    assertThat(xml, hasXPath("//submission/@accno", equalTo(accNo)))
    assertXmlAttributes(
        xml,
        "//submission",
        listOf(Attribute("Title", "venous blood, ∆Monocyte"), Attribute("ReleaseDate", "2021-02-12"))
    )

    assertXmlSection(xml, "//submission/section", allInOneRootSection())
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[2]/@reference", equalTo("true")))
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[3]/nmqual/name", equalTo("Tissue")))
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[3]/nmqual/value", equalTo("Blood")))
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[3]/valqual/name", equalTo("Ontology")))
    assertThat(xml, hasXPath("//submission/section/attributes/attribute[3]/valqual/value", equalTo("UBERON")))

    assertXmlLink(xml, "//submission/section/links/link", allInOneRootSectionLink())
    assertXmlFile(xml, "//submission/section/files/file[1]", allInOneRootSectionFile())
    assertXmlFilesTable(xml, "//submission/section/files/table", allInOneRootSectionFilesTable())

    assertXmlSection(xml, "//submission/section/subsections/section", allInOneSubsection())
    assertXmlLinksTable(xml, "//submission/section/subsections/section/links/table", allInOneSubSectionLinksTable())
    assertXmlSectionsTable(xml, "//submission/section/subsections/table", allInOneSubSectionsTable())
}

private fun assertXmlSection(xml: String, xPath: String, section: Section) {
    assertThat(xml, hasXPath("$xPath/@accno", equalTo(section.accNo)))
    assertThat(xml, hasXPath("$xPath/@type", equalTo(section.type)))
    assertXmlAttributes(xml, xPath, section.attributes)
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

private fun assertXmlFile(xml: String, xPath: String, file: File) {
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
