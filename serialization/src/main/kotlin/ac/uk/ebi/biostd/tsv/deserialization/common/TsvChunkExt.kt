package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.SECTION_TABLE_OP
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ebi.ac.uk.util.collections.firstOrElse
import ebi.ac.uk.util.collections.secondOrElse

fun TsvChunk.findId(): String? = header.secondOrElse(value = null)
fun TsvChunk.getIdOrElse(exception: Exception): String = header.secondOrElse { throw exception }

fun TsvChunk.getType() = if (isSectionTable()) header.first().substringBefore(SECTION_TABLE_OP) else header.first()
fun TsvChunk.getTypeOrElse(exception: Exception) = header.firstOrElse { throw exception }

fun TsvChunk.isSectionTable() = header.first().matches(".+\\[.*]".toRegex())
