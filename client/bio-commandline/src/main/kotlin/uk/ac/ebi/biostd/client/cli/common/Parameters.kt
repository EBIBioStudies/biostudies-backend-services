package uk.ac.ebi.biostd.client.cli.common

const val FILES_SEPARATOR = ','

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
    const val FILE_MODE = "Indicates the mode used to process the files. Valid values are COPY or MOVE"
    const val PREFERRED_SOURCE = "Indicates the preferred files source. Valid values are SUBMISSION or USER_SPACE"
    const val FILE_LIST_PATH = "Path to the file list to be validated. The path is relative to the user folder"
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
    const val ASYNC = "Indicates whether or not the migration should be processed in async mode"
}

internal object GrantPermissionParameters {
    const val TARGET_USER = "BioStudies user to grant permission"
    const val ACCESS_TYPE = "Access Type to grant to the target user"
    const val ACCESS_TAG_NAME = "Tag name of a collection"
}
