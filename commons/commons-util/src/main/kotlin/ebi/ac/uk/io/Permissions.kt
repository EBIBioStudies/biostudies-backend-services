package ebi.ac.uk.io

import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

enum class Permissions(val stringValue: String) {
    ALL_CAN_READ("rwxr-xr-x"),
    ALL_GROUP("rwxrwx---"),
    READ_ONLY_GROUP("rwxr-x---"),
    GROUP_EXECUTE("rwx--x---"),
    ONLY_USER("rwx------")
}

fun Permissions.toPosix(): Set<PosixFilePermission> = PosixFilePermissions.fromString(stringValue)

fun Permissions.toPosixNoExecute(): Set<PosixFilePermission> =
    PosixFilePermissions.fromString(stringValue.replace("x", "-"))