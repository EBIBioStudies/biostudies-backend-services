package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.SECTION_TABLE_OP
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.util.collections.findSecond
import ebi.ac.uk.util.collections.second

internal fun TsvChunk.findId(): String? = if (header.findSecond().isNullOrEmpty()) null else header.second()

internal fun TsvChunk.getIdOrElse(exception: Exception): String = header.second() ?: throw exception

internal fun TsvChunk.getTypeOrElse(exception: Exception): String = header.first() ?: throw exception

internal fun TsvChunk.isSectionTable(): Boolean = header.first()?.matches(".+\\[.*]".toRegex()).orFalse()

internal fun TsvChunk.getType(): String = if (isSectionTable()) header.first()!!.substringBefore(SECTION_TABLE_OP) else header.first()!!
