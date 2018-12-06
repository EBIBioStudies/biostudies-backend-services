package ac.uk.ebi.biostd.tsv.deserialization.ext

import ac.uk.ebi.biostd.tsv.SECTION_TABLE_CL
import ac.uk.ebi.biostd.tsv.SECTION_TABLE_OP
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.util.collections.secondOrElse
import ebi.ac.uk.util.collections.thirdOrElse

fun TsvChunk.getType() = if (isSectionTable()) header.first().substringBefore(SECTION_TABLE_OP) else header.first()

fun TsvChunk.findIdentifier(): String? = header.secondOrElse(value = null)

fun TsvChunk.getIdentifier(): String = header.secondOrElse { throw Exception("") }

fun TsvChunk.isSectionTable() = header.first().matches(".+\\[.*]".toRegex())

fun TsvChunk.getParent() =
    if (isSectionTable()) header.first().substringAfter(SECTION_TABLE_OP).substringBefore(SECTION_TABLE_CL)
    else header.thirdOrElse(EMPTY)

fun TsvChunk.isSubsection() = getParent().isNotEmpty()
