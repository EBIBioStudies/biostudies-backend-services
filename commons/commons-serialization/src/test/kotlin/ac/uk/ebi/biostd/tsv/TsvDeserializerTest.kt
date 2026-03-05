package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.test.basicSubmission
import ac.uk.ebi.biostd.test.basicSubmissionWithComments
import ac.uk.ebi.biostd.test.basicSubmissionWithMultiline
import ac.uk.ebi.biostd.test.sectionWithEmptyAccParentSection
import ac.uk.ebi.biostd.test.submissionWithBlankAttribute
import ac.uk.ebi.biostd.test.submissionWithDetailedAttributes
import ac.uk.ebi.biostd.test.submissionWithEmptyAttribute
import ac.uk.ebi.biostd.test.submissionWithEmptyLinks
import ac.uk.ebi.biostd.test.submissionWithFiles
import ac.uk.ebi.biostd.test.submissionWithFilesTable
import ac.uk.ebi.biostd.test.submissionWithGenericRootSection
import ac.uk.ebi.biostd.test.submissionWithInnerSubsections
import ac.uk.ebi.biostd.test.submissionWithInnerSubsectionsTable
import ac.uk.ebi.biostd.test.submissionWithLinks
import ac.uk.ebi.biostd.test.submissionWithLinksTable
import ac.uk.ebi.biostd.test.submissionWithMultipleLineBreaks
import ac.uk.ebi.biostd.test.submissionWithNullAttribute
import ac.uk.ebi.biostd.test.submissionWithQuoteValue
import ac.uk.ebi.biostd.test.submissionWithRootSection
import ac.uk.ebi.biostd.test.submissionWithSubsection
import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ac.uk.ebi.biostd.validation.DuplicatedSectionAccNoException
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.SerializationException
import ebi.ac.uk.base.Either
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.link
import ebi.ac.uk.dsl.linksTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.sectionsTable
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ebi.ac.uk.asserts.assertThat as assertEither

class TsvDeserializerTest {
    private val deserializer = TsvDeserializer()

    @Test
    fun `basic submission`() {
        val result = deserializer.deserialize(basicSubmission().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC123") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Basic Submission")
                attribute("DataSource", "EuropePMC")
                attribute("AttachTo", "EuropePMC")
            },
        )
    }

    @Test
    fun `submission with empty attribute`() {
        val result = deserializer.deserialize(submissionWithEmptyAttribute().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC123") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Basic Submission")
                attribute("DataSource", "EuropePMC")
                attribute("Abstract", null)
            },
        )
    }

    @Test
    fun `submission with blank attribute`() {
        val result = deserializer.deserialize(submissionWithBlankAttribute().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC123") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Basic Submission")
                attribute("DataSource", "EuropePMC")
                attribute("Abstract", null)
            },
        )
    }

    @Test
    fun `submission with null attribute`() {
        val result = deserializer.deserialize(submissionWithNullAttribute().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC123") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Basic Submission")
                attribute("DataSource", "EuropePMC")
                attribute("Abstract", null)
            },
        )
    }

    @Test
    fun `submission with quoted value`() {
        val result = deserializer.deserialize(submissionWithQuoteValue().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC123") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "The \"Submission\": title.")
                attribute("Abstract", "\"The Submission\": this is description.")
                attribute("Sub-Title", "\"The Submission (quoted)\": this is description.")
                attribute("Double Quote Attribute", "\"one value\" OR \"the other\"")
            },
        )
    }

    @Test
    fun `basic submission with comments`() {
        val result = deserializer.deserialize(basicSubmissionWithComments().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC123") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Basic Submission")
                attribute("DataSource", "EuropePMC")
                attribute("AttachTo", "EuropePMC")
            },
        )
    }

    @Test
    fun `submission with multiline attribute value`() {
        val result = deserializer.deserialize(basicSubmissionWithMultiline().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC123") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "This is a really long title \n with a break line")
                attribute("Another", "another attribute")
            },
        )
    }

    @Test
    fun `detailed attributes`() {
        val result = deserializer.deserialize(submissionWithDetailedAttributes().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC124") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Submission With Detailed Attributes")

                attribute(
                    name = "Submission Type",
                    value = "RNA-seq of non coding RNA",
                    ref = false,
                    nameAttrs = mutableListOf(AttributeDetail("Seq Type", "RNA")),
                    valueAttrs = mutableListOf(AttributeDetail("Ontology", "EFO")),
                )

                attribute("affiliation", "EuropePMC", true)
            },
        )
    }

    @Test
    fun `submission with root section`() {
        val result = deserializer.deserialize(submissionWithRootSection().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC125") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Submission")

                section("Study") {
                    attribute("Title", "Test Root Section")
                    attribute("Abstract", "Test abstract")
                }
            },
        )
    }

    @Test
    fun `submission with generic root section`() {
        val result = deserializer.deserialize(submissionWithGenericRootSection().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC125") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Submission")
                section("Compound") {
                    attribute("Title", "Generic Root Section")
                }
            },
        )
    }

    @Test
    fun `submission with a section with empty string parent section accNo`() {
        val result = deserializer.deserialize(sectionWithEmptyAccParentSection().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-007A") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Submission")
                section("RootSection") {
                    attribute("Title", "Generic Root Section")
                    section("Funding") {
                        attribute("Agency", "National Support Program of China")
                    }
                }
            },
        )
    }

    @Test
    fun `submission with multiple line breaks`() {
        val result = deserializer.deserialize(submissionWithMultipleLineBreaks().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC125") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Submission")
                section("Study") {
                    attribute("Title", "Test Root Section")
                    attribute("Abstract", "Test abstract")
                }
            },
        )
    }

    @Test
    fun subsection() {
        val result = deserializer.deserialize(submissionWithSubsection().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC125") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Submission")
                section("Study") {
                    attribute("Title", "Test Root Section")
                    attribute("Abstract", "Test abstract")

                    section("Funding") {
                        accNo = "F-001"
                        attribute("Agency", "National Support Program of China")
                        attribute("Grant Id", "No. 2015BAD27B01")
                    }
                }
            },
        )
    }

    @Test
    fun `section table with no Accno`() {
        val submission =
            tsv {
                line("Submission", "S-STBL123")
                line("ReleaseDate", "2023-02-12")
                line("Title", "Test Section Table")
                line()

                line("Study")
                line()

                line("Data[]", "Title")
                line("", "Group 1")
                line()
            }.toString()

        val result = deserializer.deserialize(submission.toString())
        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-STBL123") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Section Table")

                section("Study") {
                    sectionsTable {
                        section("Data") {
                            attribute("Title", "Group 1")
                        }
                    }
                }
            },
        )
    }

    @Test
    fun `section table with trailing tabs`() {
        val submission =
            tsv {
                line("Submission", "S-STBL123")
                line("ReleaseDate", "2023-02-12")
                line("Title", "Test Section Table")
                line()

                line("Study")
                line()

                line("Data[]", "Title", "", "")
                line("", "Group 1", "")
                line()
            }.toString()

        val result = deserializer.deserialize(submission.toString())
        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-STBL123") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Section Table")

                section("Study") {
                    sectionsTable {
                        section("Data") {
                            attribute("Title", "Group 1")
                        }
                    }
                }
            },
        )
    }

    @Test
    fun `inner subsections`() {
        val result = deserializer.deserialize(submissionWithInnerSubsections().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC125") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Submission")
                section("Study") {
                    attribute("Title", "Test Root Section")
                    attribute("Abstract", "Test abstract")

                    section("Funding") {
                        accNo = "F-001"
                        attribute("Agency", "National Support Program of China")
                        attribute("Grant Id", "No. 2015BAD27B01")

                        section("Expense") {
                            accNo = null
                            attribute("Description", "Travel")
                        }

                        section("Expense") {
                            accNo = "E-001"
                            attribute("Description", "Accommodation")
                        }
                    }

                    section("Funding") {
                        accNo = "F-002"
                        attribute("Agency", "National Support Program of Japan")
                        attribute("Grant Id", "No. 2015BAD27A03")

                        section("Funding Protocols") {
                            accNo = null
                            attribute("Description", "Submission funding protocols")
                        }
                    }
                }
            },
        )
    }

    @Test
    fun `inner subsections table`() {
        val result = deserializer.deserialize(submissionWithInnerSubsectionsTable().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC125") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Submission")
                section("Study") {
                    attribute("Title", "Test Root Section")
                    attribute("Abstract", "Test abstract")

                    section("Funding") {
                        accNo = "F-001"
                        attribute("Agency", "National Support Program of China")
                        attribute("Grant Id", "No. 2015BAD27B01")
                    }

                    section("Study") {
                        accNo = "S-001"
                        attribute("Type", "Imaging")

                        sectionsTable {
                            section("Sample") {
                                accNo = "SMP-1"
                                parentAccNo = "S-001"
                                attribute("Title", "Sample1")
                                attribute("Desc", "Measure 1")
                            }

                            section("Sample") {
                                accNo = "SMP-2"
                                parentAccNo = "S-001"
                                attribute("Title", "Sample2")
                                attribute("Desc", "Measure 2")
                            }
                        }
                    }
                }
            },
        )
    }

    @Test
    fun links() {
        val result = deserializer.deserialize(submissionWithLinks().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC125") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Submission")

                section("Study") {
                    attribute("Title", "Test Root Section")
                    attribute("Abstract", "Test abstract")

                    link("http://arandomsite.org")
                    link("http://completelyunrelatedsite.org")
                }
            },
        )
    }

    @Test
    fun `links table with attribute details`() {
        val result = deserializer.deserialize(submissionWithLinksTable().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC125") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Submission")

                section("Study") {
                    attribute("Title", "Test Root Section")
                    attribute("Abstract", "Test abstract")

                    linksTable {
                        link("AF069309") {
                            attribute(
                                name = "Type",
                                value = "gen",
                                valueAttrs = mutableListOf(AttributeDetail("Ontology", "EFO")),
                                nameAttrs = mutableListOf(AttributeDetail("TermId", "EFO_0002768")),
                            )
                        }

                        link("AF069123") {
                            attribute(
                                name = "Type",
                                value = "gen",
                                valueAttrs = mutableListOf(AttributeDetail("Ontology", "EFO")),
                                nameAttrs = mutableListOf(AttributeDetail("TermId", "EFO_0002769")),
                            )
                        }
                    }
                }
            },
        )
    }

    @Test
    fun `link with empty url`() {
        val submission = submissionWithEmptyLinks()
        val exception = assertThrows<SerializationException> { deserializer.deserialize(submission.toString()) }
        val errors = exception.errors.entries().map { it.value.cause }
        assertThat(errors).hasSize(1)
        assertThat(errors.first()).hasMessage("Link Url is required. Element was not created.")
    }

    @Test
    fun files() {
        val result = deserializer.deserialize(submissionWithFiles().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC125") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Submission")

                section("Study") {
                    attribute("Title", "Test Root Section")
                    attribute("Abstract", "Test abstract")

                    file("12870_2017_1225_MOESM10_ESM.docx")
                    file("12870_2017_1225_MOESM1_ESM.docx")
                    file("inner/folder")
                }
            },
        )
    }

    @Test
    fun `files table`() {
        val result = deserializer.deserialize(submissionWithFilesTable().toString())

        assertThat(result).usingRecursiveComparison().isEqualTo(
            submission("S-EPMC125") {
                attribute("ReleaseDate", "2023-02-12")
                attribute("Title", "Test Submission")

                section("Study") {
                    attribute("Title", "Test Root Section")
                    attribute("Abstract", "Test abstract")

                    filesTable {
                        file("Abstract.pdf") {
                            attribute("Description", "An abstract file")
                            attribute("Usage", "Testing")
                        }

                        file("SuperImportantFile1.docx") {
                            attribute("Description", "A super important file")
                            attribute("Usage", "Important stuff")
                        }

                        file("inner/folder") {
                            attribute("Description", "A super important folder")
                            attribute("Usage", "Important inner folder")
                        }
                    }
                }
            },
        )
    }

    @Test
    fun `duplicated section accNo`() {
        val submission =
            tsv {
                line("Submission", "E-MTAB-8568")
                line("ReleaseDate", "2023-02-12")
                line("Title", "Duplicated Section AccNo")
                line()

                line("Study", "s-E-MTAB-8568")
                line("Description", "A description")
                line()

                line("Author", "s-E-MTAB-8568")
                line("Name", "Connor Rogerson")
                line()
            }

        val exception = assertThrows<SerializationException> { deserializer.deserialize(submission.toString()) }
        val errorCause =
            exception.errors
                .entries()
                .first()
                .value.cause

        assertThat(errorCause).isInstanceOf(DuplicatedSectionAccNoException::class.java)
        assertThat(errorCause.message).isEqualTo("A section with accNo s-E-MTAB-8568 already exists")
    }

    @Test
    fun `submission without ReleaseDate`() {
        val submission =
            tsv {
                line("Submission", "S-EPMC123")
                line("Title", "Submission without ReleaseDate")
                line()
            }

        val exception = assertThrows<SerializationException> { deserializer.deserialize(submission.toString()) }
        val errorCause =
            exception.errors
                .entries()
                .first()
                .value.cause

        assertThat(errorCause).isInstanceOf(InvalidElementException::class.java)
        assertThat(errorCause.message).isEqualTo("ReleaseDate is required. Element was not created.")
    }

    @Test
    fun `submission with empty ReleaseDate`() {
        val submission =
            tsv {
                line("Submission", "S-EPMC123")
                line("ReleaseDate")
                line("Title", "Submission without ReleaseDate")
                line()
            }

        val exception = assertThrows<SerializationException> { deserializer.deserialize(submission.toString()) }
        val errorCause =
            exception.errors
                .entries()
                .first()
                .value.cause

        assertThat(errorCause).isInstanceOf(InvalidElementException::class.java)
        assertThat(errorCause.message).isEqualTo("ReleaseDate is required. Element was not created.")
    }

    @Nested
    inner class TablesAttributes {
        @Test
        fun `links table with empty-null attribute`() {
            fun assertLinksTable(links: MutableList<Either<Link, LinksTable>>) {
                assertThat(links).hasSize(1)
                assertEither(links.first()).hasRightValueSatisfying {
                    assertThat(it.elements).hasSize(1)
                    val link = it.elements.first()
                    assertThat(link.url).isEqualTo("Link1")
                    val linkAttributes = link.attributes
                    assertThat(linkAttributes).hasSize(2)
                    assertThat(linkAttributes.first()).isEqualTo(
                        Attribute(
                            name = "Empty Attribute",
                            value = null,
                            nameAttrs = mutableListOf(AttributeDetail("TermId", "EFO_0002768")),
                            valueAttrs = mutableListOf(AttributeDetail("Ontology", "EFO")),
                        ),
                    )
                    assertThat(linkAttributes.second()).isEqualTo(Attribute("Null Attribute", null))
                }
            }

            val submission =
                submissionWithRootSection().apply {
                    line("Links", "Empty Attribute", "(TermId)", "[Ontology]", "Null Attribute")
                    line("Link1", "", "EFO_0002768", "EFO")
                    line()
                }

            val result = deserializer.deserialize(submission.toString())

            assertSubmissionWithRootSection(result)
            assertLinksTable(result.section.links)
        }

        @Test
        fun `files table with empty-null attribute`() {
            fun assertFilesTable(files: MutableList<Either<BioFile, FilesTable>>) {
                assertThat(files).hasSize(1)
                assertEither(files.first()).hasRightValueSatisfying {
                    assertThat(it.elements).hasSize(1)
                    val file = it.elements.first()
                    val attributes = file.attributes
                    assertThat(attributes).hasSize(2)
                    assertThat(attributes.first()).isEqualTo(
                        Attribute(
                            name = "Empty Attribute",
                            value = null,
                            nameAttrs = mutableListOf(AttributeDetail("TermId", "EFO_0002768")),
                            valueAttrs = mutableListOf(AttributeDetail("Ontology", "EFO")),
                        ),
                    )
                    assertThat(attributes.second()).isEqualTo(Attribute("Null Attribute", null))
                }
            }

            val submission =
                submissionWithRootSection().apply {
                    line("Files", "Empty Attribute", "(TermId)", "[Ontology]", "Null Attribute")
                    line("testFile.txt", "", "EFO_0002768", "EFO")
                    line()
                }
            val result = deserializer.deserialize(submission.toString())

            assertSubmissionWithRootSection(result)
            assertFilesTable(result.section.files)
        }

        @Test
        fun `sections table with empty-null attributes`() {
            fun assertSectionsTable(sections: MutableList<Either<Section, SectionsTable>>) {
                assertThat(sections).hasSize(1)
                assertEither(sections.first()).hasRightValueSatisfying {
                    val innerSections = it.elements
                    assertThat(innerSections).hasSize(1)
                    assertThat(innerSections.first()).usingRecursiveComparison().isEqualTo(
                        Section(
                            type = "Data",
                            accNo = "DT-1",
                            attributes =
                                listOf(
                                    Attribute(
                                        name = "Empty Attr",
                                        value = null,
                                        nameAttrs = mutableListOf(AttributeDetail("TermId", "EFO_0002768")),
                                        valueAttrs = mutableListOf(AttributeDetail("NullValue", null)),
                                    ),
                                    Attribute(
                                        name = "Null Attr",
                                        value = null,
                                        nameAttrs = mutableListOf(AttributeDetail("NullName", null)),
                                        valueAttrs = mutableListOf(AttributeDetail("Ontology", "EFO")),
                                    ),
                                ),
                        ),
                    )
                }
            }

            val submission =
                submissionWithRootSection().apply {
                    line("Data[]", "Empty Attr", "(TermId)", "[NullValue]", "Null Attr", "(NullName)", "[Ontology]")
                    line("DT-1", "", "EFO_0002768", "", "", "", "EFO")
                    line()
                }
            val result = deserializer.deserialize(submission.toString())

            assertSubmissionWithRootSection(result)
            assertSectionsTable(result.section.sections)
        }

        private fun assertSubmissionWithRootSection(result: Submission) {
            assertThat(result.accNo).isEqualTo("S-EPMC125")
            assertThat(result.attributes).hasSize(2)
            assertThat(result.attributes).containsExactly(
                Attribute("ReleaseDate", "2023-02-12"),
                Attribute("Title", "Test Submission"),
            )
            val section = result.section
            assertThat(section.type).isEqualTo("Study")
            val sectionAttributes = section.attributes
            assertThat(sectionAttributes).hasSize(2)
            assertThat(sectionAttributes.first()).isEqualTo(Attribute("Title", "Test Root Section"))
            assertThat(sectionAttributes.second()).isEqualTo(Attribute("Abstract", "Test abstract"))
        }
    }
}
