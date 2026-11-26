@file:Suppress("TooManyFunctions")

package uk.ac.ebi.biostd.dsl

import uk.ac.ebi.biostd.model.Attributable
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.AttributeDetail
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.FilesTable
import uk.ac.ebi.biostd.model.Link
import uk.ac.ebi.biostd.model.LinksTable
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.model.SectionsTable
import uk.ac.ebi.biostd.model.Submission

fun Attributable.attribute(
    name: String,
    value: String?,
    ref: Boolean = false,
    valueAttrs: MutableList<AttributeDetail> = mutableListOf(),
    nameAttrs: MutableList<AttributeDetail> = mutableListOf(),
) = addAttribute(Attribute(name = name, value = value, valueAttrs = valueAttrs, reference = ref, nameAttrs = nameAttrs))

fun Submission.section(
    type: String,
    block: Section.() -> Unit,
) = apply { section = Section(type).apply(block) }

fun submission(
    accNo: String = "",
    block: Submission.() -> Unit,
): Submission = Submission(accNo).apply(block)

fun section(
    type: String,
    block: Section.() -> Unit,
): Section = Section(type).apply(block)

fun sectionsTable(block: SectionsTable.() -> Unit) = SectionsTable().apply(block)

fun FilesTable.file(
    name: String,
    block: BioFile.() -> Unit = {},
) = addRow(BioFile(name).apply(block))

fun SectionsTable.section(
    type: String,
    block: Section.() -> Unit = {},
) = addRow(Section(type).apply(block))

fun LinksTable.link(
    url: String,
    block: Link.() -> Unit = {},
) = addRow(Link(url).apply(block))

fun Section.filesTable(block: FilesTable.() -> Unit) = addFilesTable(FilesTable().apply(block))

fun Section.sectionsTable(block: SectionsTable.() -> Unit) = addSectionTable(SectionsTable().apply(block))

fun Section.linksTable(block: LinksTable.() -> Unit) = addLinksTable(LinksTable().apply(block))

fun Section.file(
    name: String,
    block: BioFile.() -> Unit = {},
) = addFile((BioFile(name).apply(block)))

fun Section.section(
    type: String,
    block: Section.() -> Unit,
) = addSection((Section(type).apply(block)))

fun Section.link(
    url: String,
    block: Link.() -> Unit = {},
) = addLink((Link(url).apply(block)))

fun link(
    url: String,
    block: Link.() -> Unit = {},
) = Link(url).apply(block)

fun linksTable(block: LinksTable.() -> Unit) = LinksTable().apply(block)

fun file(
    path: String,
    block: BioFile.() -> Unit = {},
) = BioFile(path).apply(block)

fun filesTable(block: FilesTable.() -> Unit) = FilesTable().apply(block)
