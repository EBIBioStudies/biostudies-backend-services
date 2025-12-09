package ac.uk.ebi.biostd.submission.exceptions

class InvalidMigrationTargetException : RuntimeException("The target and current storage mode must be different")
