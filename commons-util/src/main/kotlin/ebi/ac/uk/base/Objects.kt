package ebi.ac.uk.base

fun khash(vararg fields: Any?) = fields.fold(17) { result, field -> 31 * result + (field?.hashCode() ?: 0) }

