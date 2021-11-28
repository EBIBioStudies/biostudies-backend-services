package ac.uk.ebi.biostd.exception

class EmptyPageTabFileException(fileName: String) : RuntimeException("Empty page tab file: '$fileName'")
