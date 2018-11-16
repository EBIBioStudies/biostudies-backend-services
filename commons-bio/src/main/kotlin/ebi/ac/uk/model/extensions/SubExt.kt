package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.File
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.SubFields
import java.time.Instant

fun Submission.allFiles(): List<File> =
        rootSection.allFiles() + rootSection.allSections().map { it.allFiles() }.flatten()

var Submission.attachTo: String?
    get() = this[SubFields.ATTACH_TO]
    set(value) {
        value?.let { this[SubFields.ATTACH_TO] = it }
    }

var Submission.releaseTime: Instant?
    get() = this[SubFields.RELEASE_TIME]
    set(value) {
        value?.let { this[SubFields.RELEASE_TIME] = it }
    }

var Submission.title: String
    get() = this[SubFields.TITLE]
    set(value) {
        this[SubFields.TITLE] = value
    }

var Submission.rootPath: String
    get() = this[SubFields.ROOT_PATH]
    set(value) {
        this[SubFields.ROOT_PATH] = value
    }
