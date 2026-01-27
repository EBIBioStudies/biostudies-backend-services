package uk.ac.ebi.biostd.client.cli.common

internal const val LIST_SEPARATOR = ','

internal object CommonParameters {
    const val SERVER_HELP = "BioStudies host url"
    const val USER_HELP = "User that will perform the action"
    const val PASSWORD_HELP = "The user password"
    const val ON_BEHALF_HELP = "User that will be impersonated"
}

internal object SubmissionParameters {
    const val ACC_NO_HELP = "The submission accession number"
    const val ATTACHED_HELP = "Comma separated list of paths to the files referenced in the submission"
    const val INPUT_HELP = "Path to the file containing the submission page tab"
    const val PREFERRED_SOURCES = """
        Comma separated list of file sources. Valid values are FIRE, SUBMISSION and USER_SPACE.
        The order of the list indicates the priority in which the sources will be used
        """
    const val STORAGE_MODE = "Submission storage mode. Determines where submission need to be saved FIRE/NFS"
    const val FILE_LIST_PATH = "Path to the file list to be validated. The path is relative to the user folder"
    const val SPLIT_JOBS = "Indicate whether the submission should be processed in individual jobs per each stage"
    const val AWAIT = "Indicate whether to wait for the submission processing"
}

internal object MigrationParameters {
    const val ACC_NO = "Accession number of the submission to be migrated"
    const val TARGET = "Determines where submission need to be migrated to. Valid values are: FIRE/NFS"
}

internal object GrantPermissionParameters {
    const val TARGET_USER = "BioStudies user to grant permission"
    const val ACCESS_TYPE = "Access Type to grant to the target user"
    const val ACC_NO = "The accession to grant the permission to"
}

internal object RevokePermissionParameters {
    const val TARGET_USER = "BioStudies user to revoke permission"
    const val ACCESS_TYPE = "Access Type to revoke to the target user"
    const val ACC_NO = "The accession to revoke the permission to"
}

internal object TransferenceParameters {
    const val OWNER = "User that owns the submissions to be transferred"
    const val TARGET_OWNER = "User that will own the transferred submissions"
    const val USER_NAME = "Full name to be used to create the target user account in case it doesn't exist"
    const val ACC_NO_LIST = "Comma separated list of accession numbers of the submissions to be transferred"
}

internal object UpdateUserEmailParameters {
    const val CURRENT_EMAIL = "Current email address of the user to update"
    const val NEW_EMAIL = "New email address of the user to update"
}

internal object SubmissionRequestParameters {
    const val ACC_NO = "Accession number of the submission request to check the status"
    const val VERSION = "Version of the submission request to check the status"
}

internal object DoiParameters {
    const val ACC_NO = "Accession number of the submission to generate DOI"
}

internal object UploadUserFilesParameters {
    const val FILE_HELP = "Path of the file to be uploaded"
    const val REL_PATH_HELP = "Relative user folder path where the file will be located"
}

internal object DeleteUserFilesParameters {
    const val FILE_HELP = "Name of the file to be deleted"
    const val REL_PATH_HELP = "Relative user folder path where the file to delete is located"
}
