package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.Submission
import ac.uk.ebi.biostd.persistence.model.Tag
import ac.uk.ebi.biostd.persistence.model.User
import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import org.assertj.core.api.Assertions.assertThat
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal const val SECRET_KEY = "ABC-123"
internal const val SUB_ACC_NO = "Sub-Accno"
internal const val SUB_TITLE = "Study"
internal const val SUB_RELPATH = "/submission/relpath"
internal const val SUB_ROOT_PATH = "/rootpah"

internal val creationTime = OffsetDateTime.of(2018, 1, 1, 5, 10, 22, 1, ZoneOffset.UTC)
internal val modificationTime = creationTime.plusDays(1)
internal val releaseTime = modificationTime.plusDays(1)

internal val extSubmission
    get() = ExtSubmission(
        accNo = SUB_ACC_NO,
        title = SUB_TITLE,
        relPath = SUB_RELPATH,
        rootPath = SUB_ROOT_PATH,
        secretKey = SECRET_KEY,
        attributes = listOf(extAttribute),
        released = true,
        status = PROCESSED,
        modificationTime = modificationTime,
        releaseTime = releaseTime,
        creationTime = creationTime,
        tags = listOf(extTag),
        accessTags = listOf(extAccessTag),
        section = extSection
    )

internal val extTag: ExtTag
    get() = ExtTag("name", "value")

internal val extAccessTag: ExtAccessTag
    get() = ExtAccessTag("access-tag")

internal fun assertSubmission(submission: Submission, accessTags: List<AccessTag>, tags: List<Tag>, owner: User) {
    assertThat(submission.accNo).isEqualTo(SUB_ACC_NO)
    assertThat(submission.title).isEqualTo(SUB_TITLE)
    assertThat(submission.status).isEqualTo(PROCESSED)
    assertThat(submission.relPath).isEqualTo(SUB_RELPATH)
    assertThat(submission.rootPath).isEqualTo(SUB_ROOT_PATH)
    assertThat(submission.secretKey).isEqualTo(SECRET_KEY)
    assertThat(submission.creationTime).isEqualTo(creationTime.toEpochSecond())
    assertThat(submission.modificationTime).isEqualTo(modificationTime.toEpochSecond())
    assertThat(submission.releaseTime).isEqualTo(releaseTime.toEpochSecond())
    assertThat(submission.released).isTrue()

    assertThat(submission.attributes).hasSize(1)
    assertDbAttribute(submission.attributes.first(), extAttribute)

    assertThat(submission.accessTags).containsExactlyElementsOf(accessTags)
    assertThat(submission.tags).containsExactlyElementsOf(tags)
    assertThat(submission.owner).isEqualTo(owner)

    assertDbExtSection(submission.rootSection)
}
