package uk.ac.ebi.biostd.model

import uk.ac.ebi.biostd.base.EMPTY

/**
 * Represents a submission accession number which include a pattern a numeric value.
 */
data class AccNumber(val prefix: String, val numericValue: String? = null) {
    override fun toString() = "$prefix${numericValue ?: EMPTY}"
}
