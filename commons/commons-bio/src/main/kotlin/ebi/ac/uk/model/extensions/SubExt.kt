package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.util.date.asIsoTime
import ebi.ac.uk.util.date.fromIsoTime
import java.time.OffsetDateTime

/**
 * Contains accessor for attributes that has specific meaning in page tab specification.
 */

/**
 * Indicate that submission should be register for specific project.
 */
var Submission.attachTo: String?
    get() = find(SubFields.ATTACH_TO)
    set(value) {
        value?.let { this[SubFields.ATTACH_TO] = it }
    }

/**
 * Indicates when a submission was released to public access.
 */
var Submission.releaseDate: String?
    get() = find(SubFields.RELEASE_DATE)
    set(value) {
        value?.let { this[SubFields.RELEASE_DATE] = it }
    }

/**
 * Indicates when a submission was released to public access.
 */
var Submission.releaseTime: OffsetDateTime?
    get() = find(SubFields.RELEASE_TIME)?.let { fromIsoTime(it) }
    set(value) {
        value?.let { this[SubFields.RELEASE_TIME] = it.asIsoTime() }
    }

/**
 * Indicates when a submission was released to public access.
 */
var Submission.creationTime: OffsetDateTime?
    get() = find(SubFields.CREATION_TIME)?.let { fromIsoTime(it) }
    set(value) {
        value?.let { this[SubFields.CREATION_TIME] = it.asIsoTime() }
    }

/**
 * Indicates when a submission was released to public access.
 */
var Submission.modificationTime: OffsetDateTime?
    get() = find(SubFields.MODIFICATION_TIME)?.let { fromIsoTime(it) }
    set(value) {
        value?.let { this[SubFields.MODIFICATION_TIME] = it.asIsoTime() }
    }

/**
 * Indicates when a submission was released to public access.
 */
var Submission.secretKey: String?
    get() = find(SubFields.SECRET)
    set(value) {
        value?.let { this[SubFields.SECRET] = it }
    }

/**
 * Prefix set to all files in the submission.
 */
var Submission.rootPath: String?
    get() = find(SubFields.ROOT_PATH)
    set(value) {
        value?.let { this[SubFields.ROOT_PATH] = it }
    }

/**
 * Obtain the submission title attribute if present.
 */
var Submission.title: String?
    get() = find(SubFields.TITLE)
    set(value) {
        value?.let { this[SubFields.TITLE] = it }
    }

fun Submission.allFiles() = section.allFiles() + section.allSections().flatMap { it.allFiles() }
fun Submission.getSectionByType(name: String): Section = section.allSections().first { it.type == name }
fun Submission.fileListSections() = (section.allSections() + section).filterNot { it.libraryFileName.isNullOrBlank() }
fun Submission.allSections(): List<Section> = mutableListOf(section) + section.allSections()
