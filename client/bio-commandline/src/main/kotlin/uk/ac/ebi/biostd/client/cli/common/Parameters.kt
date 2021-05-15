package uk.ac.ebi.biostd.client.cli.common

const val FILES_SEPARATOR = ','

internal object CommonParameters {
    const val SERVER_HELP = "BioStudies host url"
    const val ATTACHED_HELP = "Comma separated list of paths to the files referenced in the submission"
    const val USER_HELP = "User that will perform the submission"
    const val PASSWORD_HELP = "The user password"
    const val ON_BEHALF_HELP = "The user password"
    const val INPUT_HELP = "Path to the file containing the submission page tab"
    const val ACC_NO_HELP = "The accession number of the submission"
}

internal object MigrationParameters {
    const val ACC_NO = "Accession number of the submission to migrate"
    const val SOURCE = "BioStudies environment to take the submission from"
    const val TARGET = "BioStudies environment to migrate the submission to"
    const val SOURCE_USER = "BioStudies user in the source environment"
    const val SOURCE_PASSWORD = "Password for the BioStudies user in the source environment"
    const val TARGET_USER = "BioStudies user in the target environment"
    const val TARGET_PASSWORD = "Password for the BioStudies user in the target environment"
    const val TARGET_OWNER = "Optional new owner the submission in the new environment"
}
