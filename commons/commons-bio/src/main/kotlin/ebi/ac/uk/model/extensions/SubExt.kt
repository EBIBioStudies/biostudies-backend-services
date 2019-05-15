package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields

/**
 * Contains accessor for attributes that has specific meaning in page tab specification.
 */

/**
 * Indicate that submission should be register for specific project.
 */
var Submission.attachTo: String?
    get() = this[SubFields.ATTACH_TO]
    set(value) {
        value?.let { this[SubFields.ATTACH_TO] = it }
    }

/**
 * Indicates when a submission was released to public access.
 */
var Submission.releaseDate: String?
//    get() = get<String?>(SubFields.RELEASE_DATE)?.let {
//        try {
//            LocalDate.parse(it)
//        } catch (exception: DateTimeParseException) {
//            throw InvalidDateFormatException(it)
//        }
//    }

    get() = this[SubFields.RELEASE_DATE]
    set(value) {
        value?.let { this[SubFields.RELEASE_DATE] = it }
    }

/**
 * Prefix set to all files in the submission.
 */
var Submission.rootPath: String?
    get() = this[SubFields.ROOT_PATH]
    set(value) {
        value?.let { this[SubFields.ROOT_PATH] = it }
    }

/**
 * Obtain the submission title attribute if present.
 */
var Submission.title: String?
    get() = this[SubFields.TITLE]
    set(value) {
        value?.let { this[SubFields.TITLE] = it }
    }

fun Submission.allFiles() = section.allFiles() + section.allSections().flatMap { it.allFiles() }
fun Submission.getSectionByType(name: String) = section.allSections().first { it.type == name }
fun Submission.libFileSections() = (section.allSections() + section).filterNot { it.libraryFile.isNullOrBlank() }
