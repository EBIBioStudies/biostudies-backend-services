package uk.ac.ebi.biostd.exception

class EmptyPageTabFileException(fileName: String) : RuntimeException("Empty page tab file: '$fileName'")
