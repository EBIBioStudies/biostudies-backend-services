package ac.uk.ebi.biostd.json.exception

class EmptyTableException(
    name: String,
) : RuntimeException("$name tables can't be empty")
