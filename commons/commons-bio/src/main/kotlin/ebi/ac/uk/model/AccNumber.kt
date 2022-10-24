package ebi.ac.uk.model

import ebi.ac.uk.base.EMPTY

/**
 * Represents a submission accession number which include a pattern a numeric value.
 */
data class AccNumber(val prefix: String, val numericValue: String? = null) {
    override fun toString() = "$prefix${numericValue ?: EMPTY}"
}
