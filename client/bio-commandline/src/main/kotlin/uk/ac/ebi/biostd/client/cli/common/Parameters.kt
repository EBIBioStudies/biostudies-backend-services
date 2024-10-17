package uk.ac.ebi.biostd.client.cli.common

internal const val LIST_SEPARATOR = ','

internal object CommonParameters {
    const val SERVER_HELP = "BioStudies host url"
    const val USER_HELP = "User that will perform the submission"
    const val PASSWORD_HELP = "The user password"
    const val ON_BEHALF_HELP = "The user password"
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
    const val SINGLE_JOB = "Indicate whether the submission should be processed in a single job for all the stages"
    const val AWAIT = "Indicate whether to wait for the submission processing"
}

internal object MigrationParameters {
    const val ACC_NO = "Accession number of the submission to be migrated"
    const val SOURCE = "BioStudies environment to take the submission from"
    const val TARGET = "BioStudies environment to migrate the submission to"
    const val SOURCE_USER = "BioStudies user in the source environment"
    const val SOURCE_PASSWORD = "Password for the BioStudies user in the source environment"
    const val TARGET_USER = "BioStudies user in the target environment"
    const val TARGET_PASSWORD = "Password for the BioStudies user in the target environment"
    const val TARGET_OWNER = "New owner for the submission in the target environment"
    const val ASYNC = "Indicates whether the migration should be processed in async mode"
}

internal object GrantPermissionParameters {
    const val TARGET_USER = "BioStudies user to grant permission"
    const val ACCESS_TYPE = "Access Type to grant to the target user"
    const val ACC_NO = "The accession to grant the permission to"
}

internal object FileListValidationParameters {
    const val ROOT_PATH = "Base path to search for the files in the user folder"
    const val ACC_NO = "The accNo for the submission which files will be included in the search"
}

internal object TransferenceParameters {
    const val ACC_NO = "Accession number of the submission to be transferred"
    const val TARGET = "Determines where submission need to be transferred to. Valid values are: FIRE/NFS"
}

internal object SubmissionRequestParameters {
    const val ACC_NO = "Accession number of the submission request to check the status"
    const val VERSION = "Version of the submission request to check the status"
}
