package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.SECTION_TABLE_OP
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ebi.ac.uk.util.collections.findSecond
import ebi.ac.uk.util.collections.firstOrElse
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.secondOrElse

internal fun TsvChunk.findId(): String? = if (header.findSecond().isNullOrEmpty()) null else header.second()

internal fun TsvChunk.getIdOrElse(exception: Exception): String = header.secondOrElse { throw exception }

internal fun TsvChunk.getTypeOrElse(exception: Exception) = header.firstOrElse { throw exception }

internal fun TsvChunk.isSectionTable() = header.first().matches(".+\\[.*]".toRegex())

internal fun TsvChunk.getType() = if (isSectionTable()) header.first().substringBefore(SECTION_TABLE_OP) else header.first()
