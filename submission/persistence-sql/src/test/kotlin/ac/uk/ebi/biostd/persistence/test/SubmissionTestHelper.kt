package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.model.DbAccessTag
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.DbTag
import ac.uk.ebi.biostd.persistence.model.DbUser
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.model.constants.ProcessingStatus
import org.assertj.core.api.Assertions.assertThat
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal const val SECRET_KEY = "ABC-123"
internal const val SUB_ACC_NO = "Sub-Accno"
internal const val SUB_TITLE = "Study"
internal const val SUB_RELPATH = "/submission/relpath"
internal const val SUB_ROOT_PATH = "/rootpah"
internal const val VERSION = 52
internal const val OWNER = "owner@email.com"
internal const val SUBMITTER = "submitter@email.com"

internal val creationTime = OffsetDateTime.of(2018, 1, 1, 5, 10, 22, 1, ZoneOffset.UTC)
internal val modificationTime = creationTime.plusDays(1)
internal val releaseTime = modificationTime.plusDays(1)

internal val extSubmission
    get() = ExtSubmission(
        accNo = SUB_ACC_NO,
        title = SUB_TITLE,
        owner = OWNER,
        submitter = SUBMITTER,
        relPath = SUB_RELPATH,
        rootPath = SUB_ROOT_PATH,
        secretKey = SECRET_KEY,
        attributes = listOf(extAttribute),
        released = true,
        status = ExtProcessingStatus.PROCESSED,
        version = VERSION,
        method = ExtSubmissionMethod.FILE,
        modificationTime = modificationTime,
        releaseTime = releaseTime,
        creationTime = creationTime,
        tags = listOf(extTag),
        collections = listOf(extCollection),
        section = extSection
    )

internal val extTag: ExtTag
    get() = ExtTag("name", "value")

internal val extCollection: ExtCollection
    get() = ExtCollection("access-tag")

internal fun assertSubmission(
    submission: DbSubmission,
    accessTags: List<DbAccessTag>,
    tags: List<DbTag>,
    owner: DbUser,
    submitter: DbUser
) {
    assertThat(submission.accNo).isEqualTo(SUB_ACC_NO)
    assertThat(submission.title).isEqualTo(SUB_TITLE)
    assertThat(submission.status).isEqualTo(ProcessingStatus.PROCESSED)
    assertThat(submission.relPath).isEqualTo(SUB_RELPATH)
    assertThat(submission.rootPath).isEqualTo(SUB_ROOT_PATH)
    assertThat(submission.secretKey).isEqualTo(SECRET_KEY)
    assertThat(submission.creationTime).isEqualTo(creationTime)
    assertThat(submission.modificationTime).isEqualTo(modificationTime)
    assertThat(submission.releaseTime).isEqualTo(releaseTime)
    assertThat(submission.released).isTrue()

    assertThat(submission.attributes).hasSize(1)
    assertDbAttribute(submission.attributes.first(), extAttribute)

    assertThat(submission.accessTags).containsExactlyElementsOf(accessTags)
    assertThat(submission.tags).containsExactlyElementsOf(tags)
    assertThat(submission.owner).isEqualTo(owner)
    assertThat(submission.submitter).isEqualTo(submitter)

    assertDbExtSection(submission.rootSection)
}
