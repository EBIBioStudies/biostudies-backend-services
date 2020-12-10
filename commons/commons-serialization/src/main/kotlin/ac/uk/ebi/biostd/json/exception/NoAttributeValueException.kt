package ac.uk.ebi.biostd.json.exception

class NoAttributeValueException(name: String) : RuntimeException("The value for the attribute '$name' can't be empty")
