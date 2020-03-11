package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.test.basicSubmission
import ac.uk.ebi.biostd.test.basicSubmissionWithComments
import ac.uk.ebi.biostd.test.basicSubmissionWithMultiline
import ac.uk.ebi.biostd.test.submissionWithDetailedAttributes
import ac.uk.ebi.biostd.test.submissionWithFiles
import ac.uk.ebi.biostd.test.submissionWithFilesTable
import ac.uk.ebi.biostd.test.submissionWithGenericRootSection
import ac.uk.ebi.biostd.test.submissionWithInnerSubsections
import ac.uk.ebi.biostd.test.submissionWithInnerSubsectionsTable
import ac.uk.ebi.biostd.test.submissionWithLinks
import ac.uk.ebi.biostd.test.submissionWithLinksTable
import ac.uk.ebi.biostd.test.submissionWithMultipleLineBreaks
import ac.uk.ebi.biostd.test.submissionWithRootSection
import ac.uk.ebi.biostd.test.submissionWithSectionsTable
import ac.uk.ebi.biostd.test.submissionWithSubsection
import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ac.uk.ebi.biostd.validation.DuplicatedSectionAccNoException
import ac.uk.ebi.biostd.validation.SerializationException
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.line
import ebi.ac.uk.dsl.link
import ebi.ac.uk.dsl.linksTable
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.sectionsTable
import ebi.ac.uk.dsl.tsv
import ebi.ac.uk.model.AttributeDetail
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TsvDeserializerTest {
    private val deserializer = TsvDeserializer()

    @Test
    fun `basic submission`() {
        val result = deserializer.deserialize(basicSubmission().toString())

        assertThat(result).isEqualTo(submission("S-EPMC123") {
            attribute("Title", "Basic Submission")
            attribute("DataSource", "EuropePMC")
            attribute("AttachTo", "EuropePMC")
        })
    }

    @Test
    fun `basic submission with comments`() {
        val result = deserializer.deserialize(basicSubmissionWithComments().toString())

        assertThat(result).isEqualTo(submission("S-EPMC123") {
            attribute("Title", "Basic Submission")
            attribute("DataSource", "EuropePMC")
            attribute("AttachTo", "EuropePMC")
        })
    }

    @Test
    fun `submission with multiline attribute value`() {
        val result = deserializer.deserialize(basicSubmissionWithMultiline().toString())

        assertThat(result).isEqualTo(submission("S-EPMC123") {
            attribute("Title", "This is a really long title \n with a break line")
        })
    }

    @Test
    fun `detailed attributes`() {
        val result = deserializer.deserialize(submissionWithDetailedAttributes().toString())

        assertThat(result).isEqualTo(submission("S-EPMC124") {
            attribute("Title", "Submission With Detailed Attributes")

            attribute(
                "Submission Type",
                "RNA-seq of non coding RNA",
                false,
                mutableListOf(AttributeDetail("Ontology", "EFO")),
                mutableListOf(AttributeDetail("Seq Type", "RNA")))

            attribute("affiliation", "EuropePMC", true)
        })
    }

    @Test
    fun `submission with root section`() {
        val result = deserializer.deserialize(submissionWithRootSection().toString())

        assertThat(result).isEqualTo(submission("S-EPMC125") {
            attribute("Title", "Test Submission")

            section("Study") {
                attribute("Title", "Test Root Section")
                attribute("Abstract", "Test abstract")
            }
        })
    }

    @Test
    fun `submission with generic root section`() {
        val result = deserializer.deserialize(submissionWithGenericRootSection().toString())

        assertThat(result).isEqualTo(submission("S-EPMC125") {
            attribute("Title", "Test Submission")
            section("Compound") {
                attribute("Title", "Generic Root Section")
            }
        })
    }

    @Test
    fun `submission with multiple line breaks`() {
        val result = deserializer.deserialize(submissionWithMultipleLineBreaks().toString())

        assertThat(result).isEqualTo(submission("S-EPMC125") {
            attribute("Title", "Test Submission")

            section("Study") {
                attribute("Title", "Test Root Section")
                attribute("Abstract", "Test abstract")
            }
        })
    }

    @Test
    fun `submission with sections table`() {
        val result = deserializer.deserialize(submissionWithSectionsTable().toString())

        assertThat(result).isEqualTo(submission("S-EPMC125") {
            attribute("Title", "Test Submission")

            section("Study") {
                attribute("Title", "Test Root Section")
                attribute("Abstract", "Test abstract")

                sectionsTable {
                    section("Data") {
                        accNo = "DT-1"
                        attribute("Title", "Data 1")
                        attribute("Desc", "Group 1")
                    }

                    section("Data") {
                        accNo = "DT-2"
                        attribute("Title", "Data 2")
                        attribute("Desc", "Group 2")
                    }
                }
            }
        })
    }

    @Test
    fun subsection() {
        val result = deserializer.deserialize(submissionWithSubsection().toString())

        assertThat(result).isEqualTo(submission("S-EPMC125") {
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
        })
    }

    @Test
    fun `inner subsections`() {
        val result = deserializer.deserialize(submissionWithInnerSubsections().toString())

        assertThat(result).isEqualTo(submission("S-EPMC125") {
            attribute("Title", "Test Submission")

            section("Study") {
                attribute("Title", "Test Root Section")
                attribute("Abstract", "Test abstract")

                section("Funding") {
                    accNo = "F-001"
                    attribute("Agency", "National Support Program of China")
                    attribute("Grant Id", "No. 2015BAD27B01")

                    section("Expense") {
                        accNo = "E-001"
                        attribute("Description", "Travel")
                    }
                }

                section("Funding") {
                    accNo = "F-002"
                    attribute("Agency", "National Support Program of Japan")
                    attribute("Grant Id", "No. 2015BAD27A03")
                }
            }
        })
    }

    @Test
    fun `inner subsections table`() {
        val result = deserializer.deserialize(submissionWithInnerSubsectionsTable().toString())

        assertThat(result).isEqualTo(submission("S-EPMC125") {
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
        })
    }

    @Test
    fun links() {
        val result = deserializer.deserialize(submissionWithLinks().toString())

        assertThat(result).isEqualTo(submission("S-EPMC125") {
            attribute("Title", "Test Submission")

            section("Study") {
                attribute("Title", "Test Root Section")
                attribute("Abstract", "Test abstract")

                link("http://arandomsite.org")
                link("http://completelyunrelatedsite.org")
            }
        })
    }

    @Test
    fun `links table with attribute details`() {
        val result = deserializer.deserialize(submissionWithLinksTable().toString())

        assertThat(result).isEqualTo(submission("S-EPMC125") {
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
                            nameAttrs = mutableListOf(AttributeDetail("TermId", "EFO_0002768")))
                    }

                    link("AF069123") {
                        attribute(
                            name = "Type",
                            value = "gen",
                            valueAttrs = mutableListOf(AttributeDetail("Ontology", "EFO")),
                            nameAttrs = mutableListOf(AttributeDetail("TermId", "EFO_0002769")))
                    }
                }
            }
        })
    }

    @Test
    fun files() {
        val result = deserializer.deserialize(submissionWithFiles().toString())

        assertThat(result).isEqualTo(submission("S-EPMC125") {
            attribute("Title", "Test Submission")

            section("Study") {
                attribute("Title", "Test Root Section")
                attribute("Abstract", "Test abstract")

                file("12870_2017_1225_MOESM10_ESM.docx")
                file("12870_2017_1225_MOESM1_ESM.docx")
            }
        })
    }

    @Test
    fun `files table`() {
        val result = deserializer.deserialize(submissionWithFilesTable().toString())

        assertThat(result).isEqualTo(submission("S-EPMC125") {
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
                }
            }
        })
    }

    @Test
    fun `duplicated section accNo`() {
        val submission = tsv {
            line("Submission", "E-MTAB-8568")
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
        val errorCause = exception.errors.entries().first().value.cause

        assertThat(errorCause).isInstanceOf(DuplicatedSectionAccNoException::class.java)
        assertThat(errorCause.message).isEqualTo("A section with accNo s-E-MTAB-8568 already exists")
    }
}
