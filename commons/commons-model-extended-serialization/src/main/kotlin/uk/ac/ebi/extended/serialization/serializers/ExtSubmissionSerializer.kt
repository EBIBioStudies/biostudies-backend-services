package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.web.util.UriUtils.encodePath
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ACCESS_TAGS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ACC_NO
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.COLLECTIONS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.CREATION_TIME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.DOI
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.METHOD
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.MOD_TIME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.OWNER
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.PAGE_TAB_FILES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.RELEASED
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.RELEASE_TIME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.REL_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ROOT_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.SCHEMA_VERSION
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.SECRET_KEY
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.SECTION
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.STATS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.STORAGE_MODE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.SUBMITTER
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.TAGS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.TITLE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.VERSION
import java.nio.charset.StandardCharsets.UTF_8

const val STATS_URL = "stats/submission"

class ExtSubmissionSerializer : JsonSerializer<ExtSubmission>() {
    override fun serialize(
        submission: ExtSubmission,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        ExtSectionSerializer.parentAccNo = submission.accNo

        gen.writeStartObject()
        gen.writeStringField(ACC_NO, submission.accNo)
        gen.writeNumberField(VERSION, submission.version)
        gen.writeStringField(SCHEMA_VERSION, submission.schemaVersion)
        gen.writeStringField(OWNER, submission.owner)
        gen.writeStringField(SUBMITTER, submission.submitter)
        gen.writeStringField(TITLE, submission.title)
        gen.writeStringField(DOI, submission.doi)
        gen.writeStringField(METHOD, submission.method.name)
        gen.writeStringField(REL_PATH, submission.relPath)
        gen.writeStringField(ROOT_PATH, submission.rootPath)
        gen.writeBooleanField(RELEASED, submission.released)
        gen.writeStringField(SECRET_KEY, submission.secretKey)
        gen.writeObjectField(RELEASE_TIME, submission.releaseTime)
        gen.writeObjectField(MOD_TIME, submission.modificationTime)
        gen.writeObjectField(CREATION_TIME, submission.creationTime)
        gen.writeObjectField(SECTION, submission.section)
        gen.writeObjectField(ATTRIBUTES, submission.attributes)
        gen.writeObjectField(TAGS, submission.tags)
        gen.writeObjectField(COLLECTIONS, submission.collections)
        gen.writeObjectField(STATS, statsUrl(submission.accNo))
        gen.writeObjectField(ACCESS_TAGS, getAccessTag(submission))
        gen.writeObjectField(PAGE_TAB_FILES, submission.pageTabFiles)
        gen.writeObjectField(STORAGE_MODE, submission.storageMode.value)
        gen.writeEndObject()
    }

    private fun getAccessTag(submission: ExtSubmission): List<ExtAccessTag> {
        val projects = submission.collections.map { ExtAccessTag(it.accNo) }.plus(ExtAccessTag(submission.owner))
        return if (submission.released) projects.plus(ExtAccessTag("Public")) else projects
    }

    private fun statsUrl(accNo: String): String = encodePath("/$STATS_URL/$accNo", UTF_8)
}
