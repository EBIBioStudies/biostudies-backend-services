package ebi.ac.uk.model.constans

private const val LOOKAHEAD_LIMITER = "(?=%1\$s)"

val TABLE_REGEX = ".+\\[(.*)]".toRegex()
val SUB_SEPARATOR = LOOKAHEAD_LIMITER.format("Submission\t").toRegex()