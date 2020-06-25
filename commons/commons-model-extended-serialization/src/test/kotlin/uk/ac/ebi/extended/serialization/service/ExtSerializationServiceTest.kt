package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
import ebi.ac.uk.extended.model.ExtTag
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ExtSerializationServiceTest {
    private val testInstance = ExtSerializationService()

    @Test
    fun `serialize - deserialize`() {
        val time = OffsetDateTime.of(2019, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC)
        val extSubmission = ExtSubmission(
            accNo = "S-TEST123",
            version = 1,
            owner = "owner@mail.org",
            submitter = "submitter@mail.org",
            title = "Test Submission",
            method = PAGE_TAB,
            relPath = "/a/rel/path",
            rootPath = "/a/root/path",
            released = false,
            secretKey = "a-secret-key",
            status = PROCESSED,
            releaseTime = time,
            modificationTime = time,
            creationTime = time,
            attributes = listOf(ExtAttribute("AttachTo", "BioImages")),
            tags = listOf(ExtTag("component", "web")),
            accessTags = listOf(ExtAccessTag("BioImages")),
            section = ExtSection(type = "Study")
        )

        val serialized = testInstance.serialize(extSubmission)
        val deserialized = testInstance.deserialize<ExtSubmission>(serialized)

        assertThat(deserialized).isEqualTo(extSubmission)
    }
}
