package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import ebi.ac.uk.dsl.json.JsonObject
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.RequestFile
import ebi.ac.uk.extended.model.StorageMode
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.TYPE
import uk.ac.ebi.extended.serialization.constants.ExtType
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(TemporaryFolderExtension::class)
class ExtSubmissionSerializerTest {
    private val testInstance = ObjectMapper().registerModule(createModule())

    @Test
    fun `serialize basic section when not released`() {
        val extendedSubmission = createTestSubmission(released = false)
        val expectedJson = expectedJsonSubmission(released = false).toString()

        assertThat(testInstance.writeValueAsString(extendedSubmission)).isEqualToIgnoringWhitespace(expectedJson)
        assertThat(ExtSectionSerializer.parentAccNo).isEqualTo("S-TEST1")
    }

    @Test
    fun `serialize basic section when released`() {
        val extendedSubmission = createTestSubmission(released = true)
        val expectedJson = expectedJsonSubmission(released = true).toString()

        assertThat(testInstance.writeValueAsString(extendedSubmission)).isEqualToIgnoringWhitespace(expectedJson)
        assertThat(ExtSectionSerializer.parentAccNo).isEqualTo("S-TEST1")
    }

    companion object {
        fun createModule(): Module {
            val module = SimpleModule()
            module.addSerializer(ExtSubmission::class.java, ExtSubmissionSerializer())
            module.addSerializer(ExtSection::class.java, DummySectionSerializer)
            module.addSerializer(ExtFile::class.java, DummyExtFileSerializer)
            module.addSerializer(OffsetDateTime::class.java, OffsetDateTimeSerializer())

            return module
        }

        private fun expectedJsonSubmission(released: Boolean): JsonObject =
            jsonObj {
                "accNo" to "S-TEST1"
                "version" to 1
                "schemaVersion" to "1.0"
                "owner" to "owner@mail.org"
                "submitter" to "submitter@mail.org"
                "title" to "Test Submission"
                "doi" to "10.983/S-TEST1"
                "method" to ExtSubmissionMethod.PAGE_TAB
                "relPath" to "/a/rel/path"
                "rootPath" to "/a/root/path"
                "released" to released
                "secretKey" to "a-secret-key"
                "releaseTime" to "2019-09-21T10:30:34.000000015Z"
                "submissionTime" to "2020-09-21T09:30:34.000000015Z"
                "modificationTime" to "2020-09-21T10:30:34.000000015Z"
                "creationTime" to "2018-09-21T10:30:34.000000015Z"
                "section" to
                    jsonObj {
                        "type" to "Study"
                    }
                "attributes" to
                    jsonArray(
                        jsonObj {
                            "name" to "AttachTo"
                            "value" to "BioImages"
                            "reference" to false
                            "nameAttrs" to jsonArray()
                            "valueAttrs" to jsonArray()
                        },
                    )
                "tags" to
                    jsonArray(
                        jsonObj {
                            "name" to "component"
                            "value" to "web"
                        },
                    )
                "collections" to
                    jsonArray(
                        jsonObj {
                            "accNo" to "BioImages"
                        },
                    )
                "stats" to "/stats/submission/S-TEST1"
                "accessTags" to
                    if (released) {
                        jsonArray(
                            jsonObj { "name" to "BioImages" },
                            jsonObj { "name" to "owner@mail.org" },
                            jsonObj { "name" to "Public" },
                        )
                    } else {
                        jsonArray(
                            jsonObj { "name" to "BioImages" },
                            jsonObj { "name" to "owner@mail.org" },
                        )
                    }
                "pageTabFiles" to
                    jsonArray(
                        jsonObj {
                            "extType" to "fireFile"
                        },
                        jsonObj {
                            "extType" to "fireFile"
                        },
                        jsonObj {
                            "extType" to "nfsFile"
                        },
                    )
                "storageMode" to "NFS"
            }

        private fun createTestSubmission(released: Boolean): ExtSubmission {
            val releaseTime = OffsetDateTime.of(2019, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
            val submissionTime = OffsetDateTime.of(2020, 9, 21, 9, 30, 34, 15, ZoneOffset.UTC)
            val modificationTime = OffsetDateTime.of(2020, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
            val creationTime = OffsetDateTime.of(2018, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)

            return ExtSubmission(
                accNo = "S-TEST1",
                version = 1,
                schemaVersion = "1.0",
                storageMode = StorageMode.NFS,
                owner = "owner@mail.org",
                submitter = "submitter@mail.org",
                title = "TestSubmission",
                doi = "10.983/S-TEST1",
                method = ExtSubmissionMethod.PAGE_TAB,
                relPath = "/a/rel/path",
                rootPath = "/a/root/path",
                released = released,
                secretKey = "a-secret-key",
                releaseTime = releaseTime,
                submissionTime = submissionTime,
                modificationTime = modificationTime,
                creationTime = creationTime,
                attributes = listOf(ExtAttribute("AttachTo", "BioImages")),
                tags = listOf(ExtTag("component", "web")),
                collections = listOf(ExtCollection("BioImages")),
                section = ExtSection(type = "Study"),
                pageTabFiles =
                    listOf(
                        FireFile("fireId", "firePath", false, "S-TEST1", "S-TEST1", "md5", 1L, FILE, listOf()),
                        FireFile("dirFireId", "dirFirePath", true, "S-TEST1", "S-TEST1", "md5", 2L, DIR, listOf()),
                        NfsFile("S-TEST1", "S-TEST1", File("anyPath"), "/test//S-TEST1", "md5", 55, listOf()),
                    ),
            )
        }
    }
}

object DummySectionSerializer : JsonSerializer<ExtSection>() {
    override fun serialize(
        section: ExtSection,
        gen: JsonGenerator,
        serializers: SerializerProvider?,
    ) {
        gen.writeStartObject()
        gen.writeStringField(TYPE, section.type)
        gen.writeEndObject()
    }
}

object DummyExtFileSerializer : JsonSerializer<ExtFile>() {
    override fun serialize(
        extFile: ExtFile,
        gen: JsonGenerator,
        serializers: SerializerProvider?,
    ) {
        gen.writeStartObject()
        when (extFile) {
            is FireFile -> gen.writeStringField(EXT_TYPE, ExtType.FireFile.type)
            is NfsFile -> gen.writeStringField(EXT_TYPE, ExtType.NfsFile.type)
            is RequestFile -> gen.writeStringField(EXT_TYPE, ExtType.RequestFile.type)
        }
        gen.writeEndObject()
    }
}
