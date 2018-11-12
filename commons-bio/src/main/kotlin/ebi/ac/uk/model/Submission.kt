package ebi.ac.uk.model

import ebi.ac.uk.base.EMPTY
import java.time.OffsetDateTime

open class Submission(
        var accNo: String,
        var rootSection: Section = Section(),
        attributes: List<Attribute> = emptyList()) : Attributable(attributes) {

    var accessTags: MutableList<String> = mutableListOf()
}

class ExtendedSubmission(
        accNo: String,
        val user: User) : Submission(accNo) {

    var relPath = EMPTY
    var released = false
    var secretKey = EMPTY

    val releaseTime = OffsetDateTime.now()
    val modificationTime = OffsetDateTime.now()
    val createTime = OffsetDateTime.now()
}
