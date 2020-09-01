package ac.uk.ebi.biostd.persistence.service.filesystem

import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

enum class SubmissionFilesPermissions(val stringValue: String) {
    ALL_CAN_READ("rwxr-xr-x"),
    ONLY_USER("rwx------"),
    READ_ONLY_GROUP("rwxr-x---")
}

fun SubmissionFilesPermissions.asFolderPermissions(): Set<PosixFilePermission> =
    PosixFilePermissions.fromString(stringValue)

fun SubmissionFilesPermissions.asFilePermissions(): Set<PosixFilePermission> =
    PosixFilePermissions.fromString(stringValue.replace("x", "-"))
