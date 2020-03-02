package ebi.ac.uk.model

import ebi.ac.uk.base.EMPTY

/**
 * Represents an submission accession number which include a pattern an a numeric value.
 */
data class AccNumber(val prefix: String, val numericValue: Long? = null) {
    override fun toString() = "$prefix${numericValue ?: EMPTY}"
}
