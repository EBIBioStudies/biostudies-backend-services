package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.pageTabFallbackPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.repositories.TagDataRepository
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ac.uk.ebi.biostd.submission.model.DoiRequest.Companion.BS_DOI_ID
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.base.Either
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.PersistedExtFile
import ebi.ac.uk.io.ext.createOrReplaceFile
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.isAuthor
import ebi.ac.uk.model.extensions.isOrganization
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Path
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpecialSubmissionAttributesTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val tagsRefRepository: TagDataRepository,
    @Autowired val toSubmissionMapper: ToSubmissionMapper,
    @Autowired val serializationService: SerializationService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init(): Unit =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)

            tagsRefRepository.save(DbTag(classifier = "classifier", name = "tag"))
        }

    @Test
    fun `15-2 submission with tags`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-TEST123")
                    line("Title", "Submission With Tags")
                    line()

                    line("Study", "SECT-001", "", "classifier:tag")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()
            val submitted = toSubmissionMapper.toSimpleSubmission(submissionRepository.getExtByAccNo("S-TEST123"))
            assertThat(submitted).isEqualTo(
                submission("S-TEST123") {
                    title = "Submission With Tags"
                    section("Study") {
                        accNo = "SECT-001"
                        tags = mutableListOf(Pair("Classifier", "Tag"))
                    }
                },
            )
        }

    @Test
    fun `15-3 new submission with sections table without elements`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-STBL123")
                    line("Title", "Test Section Table")
                    line()

                    line("Study", "SECT-001")
                    line()

                    line("Data[SECT-001]", "Title")
                    line("", "Group 1")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-STBL123")
            assertThat(savedSubmission.accNo).isEqualTo("S-STBL123")
            assertThat(savedSubmission.title).isEqualTo("Test Section Table")

            val section = savedSubmission.section
            assertThat(section.accNo).isEqualTo("SECT-001")
            assertThat(section.sections).hasSize(1)
            section.sections.first().ifRight {
                assertThat(it.sections).hasSize(1)

                val subSection = it.sections.first()
                assertThat(subSection.accNo).isNull()
                assertThat(subSection.attributes).hasSize(1)
                assertThat(subSection.attributes.first()).isEqualTo(ExtAttribute("Title", "Group 1"))
            }
        }

    @Test
    fun `15-4 new submission with empty-null attributes`() =
        runTest {
            fun assertSubmission(submission: ExtSubmission) {
                assertThat(submission.accNo).isEqualTo("S-STBL124")
                assertThat(submission.title).isEqualTo("Test Section Table")

                val submissionAttributes = submission.attributes
                assertThat(submissionAttributes).hasSize(2)
                assertThat(submissionAttributes.first())
                    .isEqualTo(ExtAttribute("Submission Empty Attribute", null))
                assertThat(submissionAttributes.second())
                    .isEqualTo(ExtAttribute("Submission Null Attribute", null))
            }

            fun assertSection(section: ExtSection) {
                assertThat(section.accNo).isEqualTo("SECT-001")

                val sectionAttributes = section.attributes
                assertThat(sectionAttributes).hasSize(2)
                assertThat(sectionAttributes.first()).isEqualTo(ExtAttribute("Section Empty Attribute", null))
                assertThat(sectionAttributes.second()).isEqualTo(ExtAttribute("Section Null Attribute", null))
            }

            fun assertLinks(links: List<Either<ExtLink, ExtLinkTable>>) {
                assertThat(links).hasSize(1)
                assertThat(links.first()).hasLeftValueSatisfying {
                    assertThat(it.url).isEqualTo("www.linkTable.com")

                    val attributes = it.attributes
                    assertThat(attributes).hasSize(2)
                    assertThat(attributes.first()).isEqualTo(ExtAttribute("Link Empty Attribute", null))
                    assertThat(attributes.second()).isEqualTo(ExtAttribute("Link Null Attribute", null))
                }
            }

            fun assertFiles(
                files: List<Either<ExtFile, ExtFileTable>>,
                fileName: String,
            ) {
                assertThat(files).hasSize(1)
                assertThat(files.first()).hasLeftValueSatisfying {
                    require(it is PersistedExtFile)
                    assertThat(it.filePath).isEqualTo(fileName)
                    assertThat(it.relPath).isEqualTo("Files/$fileName")

                    val fileAttributes = it.attributes
                    assertThat(fileAttributes).hasSize(2)
                    assertThat(fileAttributes.first()).isEqualTo(ExtAttribute("File Empty Attribute", null))
                    assertThat(fileAttributes.second()).isEqualTo(ExtAttribute("File Null Attribute", null))
                }
            }

            fun assertSubSections(sections: List<Either<ExtSection, ExtSectionTable>>) {
                assertThat(sections).hasSize(1)
                assertThat(sections.first()).hasLeftValueSatisfying {
                    val attributes = it.attributes
                    assertThat(attributes.first()).isEqualTo(ExtAttribute("SubSection Empty Attribute", null))
                    assertThat(attributes.second()).isEqualTo(ExtAttribute("SubSection Null Attribute", null))
                }
            }

            val fileName = "DataFile.txt"
            webClient.uploadFile(ITestListener.tempFolder.createOrReplaceFile(fileName))

            val submission =
                tsv {
                    line("Submission", "S-STBL124")
                    line("Title", "Test Section Table")
                    line("Submission Empty Attribute", "")
                    line("Submission Null Attribute")
                    line()

                    line("Study", "SECT-001")
                    line("Section Empty Attribute", "")
                    line("Section Null Attribute")
                    line()

                    line("Link", "www.linkTable.com")
                    line("Link Empty Attribute", "")
                    line("Link Null Attribute")
                    line()

                    line("File", fileName)
                    line("File Empty Attribute", "")
                    line("File Null Attribute")
                    line()

                    line("SubSection", "F-001")
                    line("SubSection Empty Attribute", "")
                    line("SubSection Null Attribute")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-STBL124")

            assertSubmission(savedSubmission)
            assertSection(savedSubmission.section)
            assertLinks(savedSubmission.section.links)
            assertFiles(savedSubmission.section.files, fileName)
            assertSubSections(savedSubmission.section.sections)
        }

    @Test
    fun `15-5 new submission with empty-null table attributes`() =
        runTest {
            fun assertSubmission(submission: ExtSubmission) {
                assertThat(submission.accNo).isEqualTo("S-STBL124")
                assertThat(submission.title).isEqualTo("Test Section Table")
                assertThat(submission.section.accNo).isEqualTo("SECT-001")
            }

            fun assertLinks(links: List<Either<ExtLink, ExtLinkTable>>) {
                assertThat(links).hasSize(1)
                assertThat(links.first()).hasRightValueSatisfying { linkTable ->
                    val tableLinks = linkTable.links
                    assertThat(tableLinks).hasSize(1)

                    val tableLink = tableLinks.first()
                    assertThat(tableLink.url).isEqualTo("www.linkTable.com")

                    val attributes = tableLink.attributes
                    assertThat(attributes).hasSize(2)
                    assertThat(attributes.first()).isEqualTo(ExtAttribute("Link Empty Attribute", null))
                    assertThat(attributes.second()).isEqualTo(ExtAttribute("Link Null Attribute", null))
                }
            }

            fun assertFiles(
                files: List<Either<ExtFile, ExtFileTable>>,
                fileName: String,
            ) {
                assertThat(files).hasSize(1)
                assertThat(files.first()).hasRightValueSatisfying { fileTable ->
                    val tableFile = fileTable.files
                    assertThat(tableFile).hasSize(1)

                    val file = tableFile.first() as PersistedExtFile
                    assertThat(file.filePath).isEqualTo(fileName)
                    assertThat(file.relPath).isEqualTo("Files/$fileName")

                    val attributes = file.attributes
                    assertThat(attributes).hasSize(2)
                    assertThat(attributes.first()).isEqualTo(ExtAttribute("File Empty Attribute", null))
                    assertThat(attributes.second()).isEqualTo(ExtAttribute("File Null Attribute", null))
                }
            }

            fun assertSubSections(sections: List<Either<ExtSection, ExtSectionTable>>) {
                assertThat(sections).hasSize(1)
                assertThat(sections.first()).hasRightValueSatisfying { sectionTable ->
                    val subSections = sectionTable.sections
                    assertThat(subSections).hasSize(1)
                    val attributes = subSections.first().attributes
                    assertThat(attributes.first()).isEqualTo(
                        ExtAttribute(
                            name = "Empty Attr",
                            value = null,
                            nameAttrs = listOf(ExtAttributeDetail("TermId", "EFO_0002768")),
                            valueAttrs = listOf(ExtAttributeDetail("NullValue", null)),
                        ),
                    )
                    assertThat(attributes.second()).isEqualTo(
                        ExtAttribute(
                            name = "Null Attr",
                            value = null,
                            nameAttrs = listOf(ExtAttributeDetail("NullName", null)),
                            valueAttrs = listOf(ExtAttributeDetail("Ontology", "EFO")),
                        ),
                    )
                }
            }

            val fileName = "DataFile.txt"
            webClient.uploadFile(ITestListener.tempFolder.createOrReplaceFile(fileName))

            val submission =
                tsv {
                    line("Submission", "S-STBL124")
                    line("Title", "Test Section Table")
                    line()

                    line("Study", "SECT-001")
                    line()

                    line("Links", "Link Empty Attribute", "Link Null Attribute")
                    line("www.linkTable.com", "")
                    line()

                    line("Files", "File Empty Attribute", "File Null Attribute")
                    line(fileName, "")
                    line()

                    line(
                        "Data[SECT-001]",
                        "Empty Attr",
                        "(TermId)",
                        "[NullValue]",
                        "Null Attr",
                        "(NullName)",
                        "[Ontology]",
                    )
                    line("DT-1", "", "EFO_0002768", "", "", "", "EFO")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-STBL124")
            assertSubmission(savedSubmission)

            val section = savedSubmission.section
            assertLinks(section.links)
            assertFiles(section.files, fileName)
            assertSubSections(section.sections)
        }

    @Test
    fun `15-6 submission with DOI`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-STBL125")
                    line("Title", "Submission with DOI")
                    line("DOI")

                    line("Study", "SECT-001")
                    line()

                    line("Author")
                    line("Name", "Jane Doe")
                    line("ORCID", "1234-5678-9101-1121")
                    line("Affiliation", "o1")
                    line()

                    line("Organization", "o1")
                    line("Name", "EMBL")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-STBL125")
            assertThat(savedSubmission.accNo).isEqualTo("S-STBL125")
            assertThat(savedSubmission.title).isEqualTo("Submission with DOI")
            assertThat(savedSubmission.doi).isEqualTo("$BS_DOI_ID/S-STBL125")
        }

    @Test
    fun `15-7 submission with DOI and incomplete name`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-STBL126")
                    line("Title", "Submission with DOI and incomplete name")
                    line("DOI")

                    line("Study", "SECT-001")
                    line()

                    line("Author")
                    line("Name", "Jane")
                    line("ORCID", "1234-5678-9101-1121")
                    line("Affiliation", "o1")
                    line()

                    line("Organization", "o1")
                    line("Name", "EMBL")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-STBL126")
            assertThat(savedSubmission.accNo).isEqualTo("S-STBL126")
            assertThat(savedSubmission.title).isEqualTo("Submission with DOI and incomplete name")
            assertThat(savedSubmission.doi).isEqualTo("$BS_DOI_ID/S-STBL126")
        }

    @Test
    fun `15-8 submission with DOI and no name`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "S-STBL127")
                    line("Title", "Submission with DOI and no name")
                    line("DOI")

                    line("Study", "SECT-001")
                    line()

                    line("Author")
                    line("P.I.", "Jane Doe")
                    line("ORCID", "1234-5678-9101-1121")
                    line("Affiliation", "o1")
                    line()

                    line("Organization", "o1")
                    line("Name", "EMBL")
                    line()
                }.toString()

            assertThat(webClient.submit(submission, TSV)).isSuccessful()

            val savedSubmission = submissionRepository.getExtByAccNo("S-STBL127")
            assertThat(savedSubmission.accNo).isEqualTo("S-STBL127")
            assertThat(savedSubmission.title).isEqualTo("Submission with DOI and no name")
            assertThat(savedSubmission.doi).isEqualTo("$BS_DOI_ID/S-STBL127")
        }

    @Test
    fun `15-12 submission with empty sections table`() =
        runTest {
            val submission =
                jsonObj {
                    "accno" to "S-STBL1212"
                    "attributes" to
                        jsonArray(
                            {
                                "name" to "Title"
                                "value" to "Submission With Empty Sections Table"
                            },
                            {
                                "name" to "ReleaseDate"
                                "value" to OffsetDateTime.now().toStringDate()
                            },
                        )
                    "section" to {
                        "accno" to "SECT-001"
                        "type" to "Study"
                        "attributes" to
                            jsonArray(
                                {
                                    "name" to "Project"
                                    "value" to "CEEHRC (McGill)"
                                },
                            )
                        "subsections" to
                            jsonArray(
                                jsonObj {
                                    "accno" to "SECT-002"
                                    "type" to "Study"
                                },
                                jsonArray({
                                    "accno" to "DT-1"
                                    "type" to "Data"
                                    "attributes" to
                                        jsonArray(
                                            {
                                                "name" to "Title"
                                                "value" to "Group 1 Transcription Data"
                                            },
                                            {
                                                "name" to "Description"
                                                "value" to "The data for zygotic transcription in mammals group 1"
                                            },
                                        )
                                }),
                                jsonArray(),
                            )
                    }
                }.toString()

            val exception = assertThrows<WebClientException> { webClient.submit(submission, JSON) }
            assertThat(exception).hasMessageContaining("Section tables can't be empty")
        }

    @Nested
    inner class DoubleBlindReview {
        @Test
        fun `15-9 private submission with double blind review`() =
            runTest {
                val submission =
                    tsv {
                        line("Submission", "S-STBL129")
                        line("Title", "Private Submission With Double Blind Review")
                        line("ReleaseDate", "2099-09-21")
                        line("ReviewType", "DoubleBlind")
                        line()

                        line("Study", "SECT-001")
                        line("Type", "Experiment")
                        line()

                        line("Author", "a1")
                        line("Name", "Jane Doe")
                        line()

                        line("Organization", "o1")
                        line("Name", "EMBL")
                        line()

                        line("Grants", "g1")
                        line("GrantNumber", "GBMF010101")
                        line()
                    }.toString()

                assertThat(webClient.submit(submission, TSV)).isSuccessful()
                val savedSubmission = submissionRepository.getExtByAccNo("S-STBL129")
                val pageTabFiles = getPageTabFilesContent(savedSubmission)
                checkReviewInfoIsHidden(pageTabFiles.first)
                checkReviewInfoIsHidden(pageTabFiles.second)

                // Verify page tab copy contains the full pagetab with all the authors/organizations info
                val jsonFallback = pageTabFallbackPath.resolve("${savedSubmission.relPath}/S-STBL129.json")
                assertThat(jsonFallback).hasContent(
                    """
                    {
                      "accno" : "S-STBL129",
                      "attributes" : [ {
                        "name" : "ReviewType",
                        "value" : "DoubleBlind"
                      }, {
                        "name" : "Title",
                        "value" : "Private Submission With Double Blind Review"
                      }, {
                        "name" : "ReleaseDate",
                        "value" : "2099-09-21"
                      } ],
                      "section" : {
                        "accno" : "SECT-001",
                        "type" : "Study",
                        "attributes" : [ {
                          "name" : "Type",
                          "value" : "Experiment"
                        } ],
                        "subsections" : [ {
                          "accno" : "a1",
                          "type" : "Author",
                          "attributes" : [ {
                            "name" : "Name",
                            "value" : "Jane Doe"
                          } ]
                        }, {
                          "accno" : "o1",
                          "type" : "Organization",
                          "attributes" : [ {
                            "name" : "Name",
                            "value" : "EMBL"
                          } ]
                        }, {
                          "accno" : "g1",
                          "type" : "Grants",
                          "attributes" : [ {
                            "name" : "GrantNumber",
                            "value" : "GBMF010101"
                          } ]
                        } ]
                      },
                      "type" : "submission"
                    }
                    """.trimIndent(),
                )

                val tsvFallback = pageTabFallbackPath.resolve("${savedSubmission.relPath}/S-STBL129.tsv")
                assertThat(tsvFallback).hasContent(
                    """
                    Submission	S-STBL129
                    ReviewType	DoubleBlind
                    Title	Private Submission With Double Blind Review
                    ReleaseDate	2099-09-21

                    Study	SECT-001
                    Type	Experiment

                    Author	a1	SECT-001
                    Name	Jane Doe

                    Organization	o1	SECT-001
                    Name	EMBL

                    Grants	g1	SECT-001
                    GrantNumber	GBMF010101
                    """.trimIndent(),
                )
            }

        @Test
        fun `15-10 private submission with different review type`() =
            runTest {
                val submission =
                    tsv {
                        line("Submission", "S-STBL1210")
                        line("Title", "Private Submission With Other Review")
                        line("ReleaseDate", "2099-09-21")
                        line("ReviewType", "Open Review")
                        line()

                        line("Study", "SECT-001")
                        line("Type", "Experiment")
                        line()

                        line("Author", "a1")
                        line("Name", "Jane Doe")
                        line()

                        line("Organization", "o1")
                        line("Name", "EMBL")
                        line()

                        line("Grants", "g1")
                        line()
                    }.toString()

                assertThat(webClient.submit(submission, TSV)).isSuccessful()

                val savedSubmission = submissionRepository.getExtByAccNo("S-STBL1210")
                val pageTabFiles = getPageTabFilesContent(savedSubmission)
                checkReviewInfoIsPresent(pageTabFiles.first)
                checkReviewInfoIsPresent(pageTabFiles.second)
            }

        @Test
        fun `15-11 public submission with double blind review`() =
            runTest {
                val submission =
                    tsv {
                        line("Submission", "S-STBL1211")
                        line("Title", "Public Submission With Double Blind Review")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line("ReviewType", "DoubleBlind")
                        line()

                        line("Study", "SECT-001")
                        line("Type", "Experiment")
                        line()

                        line("Author", "a1")
                        line("Name", "Jane Doe")
                        line()

                        line("Organization", "o1")
                        line("Name", "EMBL")
                        line()

                        line("Grants", "g1")
                        line()
                    }.toString()

                assertThat(webClient.submit(submission, TSV)).isSuccessful()

                val savedSubmission = submissionRepository.getExtByAccNo("S-STBL1211")
                val pageTabFiles = getPageTabFilesContent(savedSubmission)
                checkReviewInfoIsPresent(pageTabFiles.first)
                checkReviewInfoIsPresent(pageTabFiles.second)
            }

        private suspend fun checkReviewInfoIsHidden(pageTab: File) {
            val sub = serializationService.deserializeSubmission(pageTab)
            val sections = sub.section.allSections()
            assertThat(sections).hasSize(1)
            assertThat(sections).noneMatch { it.isAuthor() || it.isOrganization() }
        }

        private suspend fun checkReviewInfoIsPresent(pageTab: File) {
            val sub = serializationService.deserializeSubmission(pageTab)
            val sections = sub.section.allSections()
            assertThat(sections).hasSize(3)
            assertThat(sections.count { it.isAuthor() }).isEqualTo(1)
            assertThat(sections.count { it.isOrganization() }).isEqualTo(1)
        }

        private fun getPageTabFilesContent(sub: ExtSubmission): Pair<File, File> {
            val subFolder = "$submissionPath/${sub.relPath}"
            val tsv = Path.of("$subFolder/${sub.accNo}.tsv").toFile()
            val json = Path.of("$subFolder/${sub.accNo}.json").toFile()

            assertThat(tsv).exists()
            assertThat(json).exists()

            return tsv to json
        }
    }
}
