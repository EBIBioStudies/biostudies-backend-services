package ac.uk.ebi.biostd.submission.exceptions

class MissingDoiFieldException(field: String) : RuntimeException("The required DOI field '$field' could not be found")

class MissingTitleException : RuntimeException("A title is required for DOI registration")

class InvalidOrgNamesException(
    organizations: List<String>
) : RuntimeException("The following organization names are empty: ${organizations.joinToString(", ")}")

class InvalidOrgException : RuntimeException("Organizations are required to have an accession")

class InvalidAuthorNameException : RuntimeException("Authors are required to have a name")

class MissingAuthorAffiliationException : RuntimeException("Authors are required to have an affiliation")

class InvalidAuthorAffiliationException(
    author: String,
    organization: String,
) : RuntimeException("The organization '$organization' affiliated to the author '$author' could not be found")
