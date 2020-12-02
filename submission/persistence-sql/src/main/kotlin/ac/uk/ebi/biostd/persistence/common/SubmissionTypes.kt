package ac.uk.ebi.biostd.persistence.common

internal enum class SubmissionTypes(val value: String) {
    Study("Study"),
    Project("Project"),
    Array("Array"),
    Compound("Compound");
}
