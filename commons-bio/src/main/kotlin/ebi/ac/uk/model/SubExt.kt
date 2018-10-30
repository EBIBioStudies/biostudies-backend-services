package ebi.ac.uk.model

import java.time.OffsetDateTime

fun Submission.allFiles(): List<File> {
    return rootSection.allSections().map { it.allFiles() }.flatten()
}

var Submission.accNo: String
    get() = this[SubFields.ACC_NO]
    set(value) {
        this[SubFields.ACC_NO] = value
    }

var Submission.releaseTime: OffsetDateTime
    get() = this[SubFields.RELEASE_TIME]
    set(value) {
        this[SubFields.RELEASE_TIME] = value
    }

var Submission.creationTime: OffsetDateTime
    get() = this[SubFields.CREATION_TIME]
    set(value) {
        this[SubFields.CREATION_TIME] = value
    }