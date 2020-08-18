package ac.uk.ebi.biostd.persistence.integration

import java.io.File
import java.time.OffsetDateTime

interface SubmissionQueryService {
    fun getParentAccPattern(parentAccNo: String): String?

    fun isNew(accNo: String): Boolean

    fun getSecret(accNo: String): String

    fun getAccessTags(accNo: String): List<String>

    fun getReleaseTime(accNo: String): OffsetDateTime?

    fun existByAccNo(accNo: String): Boolean

    fun findCreationTime(accNo: String): OffsetDateTime?

    fun getAuthor(accNo: String): String

    fun getCurrentFolder(accNo: String): File?

    fun getOwner(accNo: String): String?
}
