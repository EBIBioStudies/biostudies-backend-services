package ebi.ac.uk.model.extensions

import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.COLLECTION_TYPE
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.util.date.asIsoTime
import ebi.ac.uk.util.date.fromIsoTime
import java.time.OffsetDateTime

const val SUBMISSION_EXTESIONS = "ebi.ac.uk.model.extensions.SubExtKt"

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
        when (value) {
            null -> attributes = attributes.filterNot { it.name == SubFields.RELEASE_DATE.value }
            else -> this[SubFields.RELEASE_DATE] = value
        }
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

/**
 * Obtain the submission DOI attribute if present.
 */
var Submission.doi: String?
    get() = find(SubFields.DOI)
    set(value) {
        value?.let { this[SubFields.DOI] = it }
    }

/**
 * Obtain the submission ReviewType attribute if present.
 */
var Submission.reviewType: String?
    get() = find(SubFields.REVIEW_TYPE)
    set(value) {
        value?.let { this[SubFields.REVIEW_TYPE] = it }
    }

/**
 * Obtain the submission accession number template if present.
 */
var Submission.accNoTemplate: String?
    get() = find(SubFields.ACC_NO_TEMPLATE)
    set(value) {
        value?.let { this[SubFields.ACC_NO_TEMPLATE] = it }
    }

val Submission.isCollection: Boolean
    get() = section.type == COLLECTION_TYPE

/**
 * Overrides the given list of attributes in the current submission.
 */
fun Submission.withAttributes(attrs: List<ExtAttributeDetail>): Submission {
    attrs.forEach { this[it.name] = it.value }
    return this
}

fun Submission.allFiles(): List<BioFile> = section.allFiles() + section.allSections().flatMap { it.allFiles() }

fun Submission.getSectionByType(name: String): Section = section.allSections().first { it.type == name }

fun Submission.fileListSections() = (section.allSections() + section).filterNot { it.fileListName == null }

fun Submission.allSections(): List<Section> = mutableListOf(section) + section.allSections()
