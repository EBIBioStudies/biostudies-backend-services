package ebi.ac.uk.model

import ebi.ac.uk.base.EMPTY

/**
 * Represents a submission accession pattern which can be used for specific project to generate submission accession number based on the same patter.
 */
class AccPattern(val prefix: String = EMPTY, val postfix: String = EMPTY) {

    override fun toString() = "$prefix,$postfix"

    override fun hashCode() = this.toString().hashCode()

    override fun equals(other: Any?) = (other is AccPattern) && (other.toString() == this.toString())
}

/**
 * Represents an submission accession number which include a pattern an a numeric value.
 */
class AccNumber(val pattern: AccPattern, val numericValue: Long) {

    override fun toString() = "${pattern.prefix}$numericValue${pattern.postfix}"
}
