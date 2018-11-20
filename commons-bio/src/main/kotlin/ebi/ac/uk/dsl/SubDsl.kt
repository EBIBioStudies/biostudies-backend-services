@file:Suppress("TooManyFunctions")

package ebi.ac.uk.dsl

import ebi.ac.uk.model.Attributable
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Attributes
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission

fun Attributable.attribute(
    name: String,
    value: String,
    ref: Boolean = false,
    valueAttrs: Attributes = mutableListOf(),
    nameAttrs: Attributes = mutableListOf()
) =
    addAttribute(Attribute(name = name, value = value, valueAttrs = valueAttrs, reference = ref, nameAttrs = nameAttrs))

fun Submission.section(block: Section.() -> Unit) = apply { section = Section().apply(block) }
fun submission(accNo: String, block: Submission.() -> Unit): Submission = Submission(accNo).apply(block)
fun section(block: Section.() -> Unit): Section = Section().apply(block)

fun FilesTable.file(name: String, block: File.() -> Unit = {}) = addRow(File(name).apply(block))
fun SectionsTable.section(block: Section.() -> Unit) = addRow(Section().apply(block))
fun LinksTable.link(url: String, block: Link.() -> Unit = {}) = addRow(Link(url).apply(block))

fun Section.filesTable(block: FilesTable.() -> Unit) = addFilesTable(FilesTable().apply(block))
fun Section.sectionsTable(block: SectionsTable.() -> Unit) = addSectionTable(SectionsTable().apply(block))
fun Section.linksTable(block: LinksTable.() -> Unit) = addLinksTable(LinksTable().apply(block))

fun Section.file(name: String, block: File.() -> Unit = {}) = addFile((File(name).apply(block)))
fun Section.section(block: Section.() -> Unit) = addSection((Section().apply(block)))
fun Section.link(url: String, block: Link.() -> Unit = {}) = addLink((Link(url).apply(block)))
